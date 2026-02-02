package com.deare.backend.domain.folder.repository;

import com.deare.backend.domain.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUser_IdAndIsDeletedFalseOrderByFolderOrderAsc(Long userId);

    long countByUser_IdAndIsDeletedFalse(Long userId);

    boolean existsByUser_IdAndNameAndIsDeletedFalse(Long userId, String name);

    @Query("""
        select coalesce(max(f.folderOrder), 0)
        from Folder f
        where f.user.id = :userId
          and f.isDeleted = false
    """)
    int findMaxFolderOrder(Long userId);
}