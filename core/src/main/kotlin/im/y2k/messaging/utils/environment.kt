package im.y2k.messaging.utils

import im.y2k.messaging.domain.Target

class Environment(
    val open: (Target) -> IO<Unit> = ::ignore1,
    val runOnMain: (() -> Unit) -> Unit = ::ignore1,
    val getPref: (String) -> IO<String?> = ::ignore1,
    val setPref: (String, String?) -> IO<Unit> = ::ignore2,
    val secureID: () -> IO<String> = ::ignore0)