package com.deare.backend.domain.folder.repository;

import com.deare.backend.domain.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUser_IdAndIsDeletedFalseOrderByFolderOrderAsc(Long userId);

    long countByUser_IdAndIsDeletedFalse(Long userId);

    boolean existsByUser_IdAndNameAndIsDeletedFalse(Long userId, String name);

    Optional<Folder> findByIdAndUser_IdAndIsDeletedFalse(Long folderId, Long userId);

    @Query("""
        select coalesce(max(f.folderOrder), 0)
        from Folder f
        where f.user.id = :userId
          and f.isDeleted = false
    """)
    int findMaxFolderOrder(Long userId);

    /**
     * 해당 유저의 모든 folder 삭제
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Folder f WHERE f.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}