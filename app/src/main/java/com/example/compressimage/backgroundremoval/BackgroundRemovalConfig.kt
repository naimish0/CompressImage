package com.example.compressimage.backgroundremoval

data class BackgroundRemovalConfig(
    val inputSize: Int = 320,
    val foregroundThreshold: Float = 0.52f,
    val transitionWidth: Float = 0.22f,
    val featherRadius: Int = 1,
    val minimumAlpha: Float = 0f,
    val maximumOutputPixels: Int = 8_000_000,
) {
    init {
        require(inputSize > 0)
        require(foregroundThreshold in 0f..1f)
        require(transitionWidth in 0.01f..1f)
        require(featherRadius >= 0)
        require(minimumAlpha in 0f..1f)
        require(maximumOutputPixels > 0)
    }
}

data class ModelMetadata(
    val modelName: String,
    val variant: String,
    val assetPath: String,
    val inputName: String,
    val outputName: String,
    val inputShape: LongArray,
    val outputShape: LongArray,
    val onnxSha256: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelMetadata) return false
        return modelName == other.modelName &&
            variant == other.variant &&
            assetPath == other.assetPath &&
            inputName == other.inputName &&
            outputName == other.outputName &&
            inputShape.contentEquals(other.inputShape) &&
            outputShape.contentEquals(other.outputShape) &&
            onnxSha256 == other.onnxSha256
    }

    override fun hashCode(): Int {
        var result = modelName.hashCode()
        result = 31 * result + variant.hashCode()
        result = 31 * result + assetPath.hashCode()
        result = 31 * result + inputName.hashCode()
        result = 31 * result + outputName.hashCode()
        result = 31 * result + inputShape.contentHashCode()
        result = 31 * result + outputShape.contentHashCode()
        result = 31 * result + onnxSha256.hashCode()
        return result
    }
}

sealed class BackgroundRemovalError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class MissingModel(cause: Throwable? = null) : BackgroundRemovalError("Background-removal model is missing.", cause)
    class ModelLoadFailed(cause: Throwable? = null) : BackgroundRemovalError("Could not initialize the background-removal model.", cause)
    class TensorContractMismatch(message: String) : BackgroundRemovalError(message)
    class DecodeFailed(cause: Throwable? = null) : BackgroundRemovalError("Could not read the selected image.", cause)
    class InsufficientMemory(cause: Throwable? = null) : BackgroundRemovalError("The image is too large to process on this device.", cause)
    class InferenceFailed(cause: Throwable? = null) : BackgroundRemovalError("Background removal failed while running the model.", cause)
    class ExportFailed(cause: Throwable? = null) : BackgroundRemovalError("Could not create the transparent PNG result.", cause)
}
