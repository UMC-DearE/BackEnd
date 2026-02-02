package com.deare.backend.domain.folder.repository;

import com.deare.backend.domain.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUser_IdAndIsDeletedFalseOrderByFolderOrderAsc(Long userId);
}