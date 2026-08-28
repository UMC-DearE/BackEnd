package com.deare.backend.global.crypto;

import java.util.List;
import java.util.Map;

record LetterContentKeyRingSecret(int currentVersion, List<Integer> readableVersions, Map<Integer, String> keys) {
}
