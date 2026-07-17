package com.rameshta.photocompressor.backgroundremoval

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import com.rameshta.photocompressor.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext
import kotlin.math.max

@Singleton
class OnnxBackgroundRemovalEngine @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : BackgroundRemovalEngine, AutoCloseable {
    private val lock = Any()
    private var environment: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var metadata: ModelMetadata? = null

    override suspend fun removeBackground(
        bitmap: Bitmap,
        config: BackgroundRemovalConfig,
    ): BackgroundMask = withContext(defaultDispatcher) {
        val loaded = loadSession(config)
        coroutineContext.ensureActive()
        val prepared = TensorImagePreprocessor.prepare(bitmap, config.inputSize)
        coroutineContext.ensureActive()
        val rawMask = runInference(
            environment = loaded.environment,
            session = loaded.session,
            inputName = loaded.metadata.inputName,
            input = prepared.buffer,
            inputSize = config.inputSize,
        )
        coroutineContext.ensureActive()
        val alpha = MaskProcessor.toAlphaMask(
            rawMask = rawMask,
            maskWidth = config.inputSize,
            maskHeight = config.inputSize,
            outputWidth = bitmap.width,
            outputHeight = bitmap.height,
            transform = prepared.transform,
            config = config,
        )
        BackgroundMask(
            alpha = alpha,
            width = bitmap.width,
            height = bitmap.height,
            metadata = loaded.metadata,
        )
    }

    private fun runInference(
        environment: OrtEnvironment,
        session: OrtSession,
        inputName: String,
        input: FloatBuffer,
        inputSize: Int,
    ): FloatArray {
        try {
            val tensor = OnnxTensor.createTensor(
                environment,
                input,
                longArrayOf(1L, 3L, inputSize.toLong(), inputSize.toLong()),
            )
            tensor.use { safeTensor ->
                session.run(mapOf(inputName to safeTensor)).use { result ->
                    val value = result[0].value
                    return MaskProcessor.flattenModelOutput(value, inputSize, inputSize)
                }
            }
        } catch (error: OrtException) {
            throw BackgroundRemovalError.InferenceFailed(error)
        } catch (error: RuntimeException) {
            throw BackgroundRemovalError.InferenceFailed(error)
        }
    }

    private fun loadSession(config: BackgroundRemovalConfig): LoadedSession {
        synchronized(lock) {
            val existingSession = session
            val existingMetadata = metadata
            if (existingSession != null && existingMetadata != null) {
                return LoadedSession(checkNotNull(environment), existingSession, existingMetadata)
            }

            val modelBytes = try {
                context.assets.open(MODEL_ASSET).use { it.readBytes() }
            } catch (error: Throwable) {
                throw BackgroundRemovalError.MissingModel(error)
            }

            val env = environment ?: OrtEnvironment.getEnvironment().also { environment = it }
            val options = OrtSession.SessionOptions().apply {
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                setIntraOpNumThreads(max(1, Runtime.getRuntime().availableProcessors() / 2))
            }
            val newSession = try {
                env.createSession(modelBytes, options)
            } catch (error: Throwable) {
                throw BackgroundRemovalError.ModelLoadFailed(error)
            }
            val newMetadata = validateContract(newSession, config.inputSize, sha256(modelBytes))
            session = newSession
            metadata = newMetadata
            return LoadedSession(env, newSession, newMetadata)
        }
    }

    private fun validateContract(
        session: OrtSession,
        inputSize: Int,
        modelSha256: String,
    ): ModelMetadata {
        val inputName = session.inputNames.firstOrNull()
            ?: throw BackgroundRemovalError.TensorContractMismatch("The model has no input tensor.")
        val outputName = session.outputNames.firstOrNull()
            ?: throw BackgroundRemovalError.TensorContractMismatch("The model has no output tensor.")
        val inputInfo = session.inputInfo[inputName]?.info as? TensorInfo
            ?: throw BackgroundRemovalError.TensorContractMismatch("The input tensor is not a tensor.")
        val outputInfo = session.outputInfo[outputName]?.info as? TensorInfo
            ?: throw BackgroundRemovalError.TensorContractMismatch("The output tensor is not a tensor.")
        if (inputInfo.type != OnnxJavaType.FLOAT) {
            throw BackgroundRemovalError.TensorContractMismatch("The model input must be FLOAT32.")
        }
        if (outputInfo.type != OnnxJavaType.FLOAT) {
            throw BackgroundRemovalError.TensorContractMismatch("The model output must be FLOAT32.")
        }
        val inputShape = inputInfo.shape
        val expectedInput = longArrayOf(1L, 3L, inputSize.toLong(), inputSize.toLong())
        if (!inputShape.contentEquals(expectedInput)) {
            throw BackgroundRemovalError.TensorContractMismatch(
                "Unexpected model input shape: ${inputShape.joinToString(prefix = "[", postfix = "]")}.",
            )
        }
        val outputShape = outputInfo.shape
        val expectedOutput = longArrayOf(1L, 1L, inputSize.toLong(), inputSize.toLong())
        if (!outputShape.contentEquals(expectedOutput)) {
            throw BackgroundRemovalError.TensorContractMismatch(
                "Unexpected model output shape: ${outputShape.joinToString(prefix = "[", postfix = "]")}.",
            )
        }
        return ModelMetadata(
            modelName = "U2-Net",
            variant = "U2-NetP",
            assetPath = MODEL_ASSET,
            inputName = inputName,
            outputName = outputName,
            inputShape = inputShape,
            outputShape = outputShape,
            onnxSha256 = modelSha256,
        )
    }

    override fun close() {
        synchronized(lock) {
            session?.close()
            session = null
            environment?.close()
            environment = null
            metadata = null
        }
    }

    private data class LoadedSession(
        val environment: OrtEnvironment,
        val session: OrtSession,
        val metadata: ModelMetadata,
    )

    private companion object {
        const val MODEL_ASSET = "models/u2netp.onnx"

        fun sha256(bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
