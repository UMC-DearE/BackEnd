package com.deare.backend.domain.letter.search;

import java.util.List;

public interface BlindIndexKeyProvider {

    VersionedBlindIndexKey currentKey(long userId);

    List<VersionedBlindIndexKey> readableKeys(long userId);
}
