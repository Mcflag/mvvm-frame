package cn.lcsw.diningpos.function.speech

data class Sound(val assetPath: String) {
    var mName: String
    var mSoundId: Int = 0

    init {
        val components = assetPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val filename = components[components.size - 1]
        if (filename.contains(".wav")) {
            mName = filename.replace(".wav", "")
        } else {
            mName = filename.replace(".mp3", "")
        }
    }
}