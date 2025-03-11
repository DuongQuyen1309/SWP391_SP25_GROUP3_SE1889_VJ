package com.demoproject.repository;

import com.demoproject.entity.Customer;
import com.demoproject.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n WHERE n.customerId = :id AND n.createdBy IN :ids")
    Page<Note> findAllNote(@Param("id") Long id, @Param("ids") List<Long> ids, Pageable pageable);

//    @Query("SELECT n FROM Note n WHERE
//    n.createdBy IN :relatedUserList AND
//            (:idFrom IS NULL OR :idTo IS NULL OR n.id BETWEEN :idFrom AND :idTo) AND
//            (:kindOfNote IS NULL OR n.kindOfNote = :kindOfNote) AND
//            (:createdDateFrom IS NULL OR :createdDateTo IS NULL OR n.createdDate BETWEEN :createdDateFrom AND :createdDateTo) AND
//            (:moneyFrom IS NULL OR :moneyTo IS NULL OR n.money BETWEEN :moneyFrom AND :moneyTo)")

    @Query("SELECT n FROM Note n WHERE (n.customerId = :id) AND (n.createdBy IN :relatedUserList) " +
            " AND (:idFrom IS NULL OR n.id >= :idFrom) " +
            " AND (:idTo IS NULL OR n.id <= :idTo) " +
            " AND (:kindOfNote IS NULL OR n.isDebt = :kindOfNote)" +
            " AND (:createdDateFrom IS NULL OR n.createdAt >= :createdDateFrom) " +
            " AND (:createdDateTo IS NULL OR n.createdAt <= :createdDateTo) " +
            " AND (:moneyFrom IS NULL OR n.money >= :moneyFrom) " +
            " AND (:moneyTo IS NULL OR n.money <= :moneyTo) " +
            " AND (:noteSearch IS NULL OR LOWER(n.note) LIKE LOWER(CONCAT('%', :noteSearch, '%')))")
    Page<Note> findNoteByAttribute(@Param("id") Long id,
                                   @Param("relatedUserList") List<Long> relatedUserList,
                                   @Param("idFrom") Long idFrom,
                                   @Param("idTo") Long idTo,
                                   @Param("kindOfNote") String kindOfNote,
                                   @Param("createdDateFrom") LocalDateTime createdDateFrom,
                                   @Param("createdDateTo") LocalDateTime createdDateTo,
                                   @Param("noteSearch") String noteSearch,
                                   @Param("moneyFrom") Integer moneyFrom,
                                   @Param("moneyTo") Integer moneyTo,
                                   Pageable pageable);

    @Query("SELECT n FROM Note n WHERE (n.customerId = :id) AND (n.storeId = :storeID) " +
            " AND (:idFrom IS NULL OR n.id >= :idFrom) " +
            " AND (:idTo IS NULL OR n.id <= :idTo) " +
            " AND (:kindOfNote IS NULL OR n.isDebt = :kindOfNote)" +
            " AND (:createdDateFrom IS NULL OR n.createdAt >= :createdDateFrom) " +
            " AND (:createdDateTo IS NULL OR n.createdAt <= :createdDateTo) " +
            " AND (:moneyFrom IS NULL OR n.money >= :moneyFrom) " +
            " AND (:moneyTo IS NULL OR n.money <= :moneyTo) " +
            " AND (:noteSearch IS NULL OR LOWER(n.note) LIKE LOWER(CONCAT('%', :noteSearch, '%')))")
    Page<Note> findNoteByAttribute(@Param("id") Long id,
                                   @Param("storeID") Long storeID,
                                   @Param("idFrom") Long idFrom,
                                   @Param("idTo") Long idTo,
                                   @Param("kindOfNote") String kindOfNote,
                                   @Param("createdDateFrom") LocalDateTime createdDateFrom,
                                   @Param("createdDateTo") LocalDateTime createdDateTo,
                                   @Param("noteSearch") String noteSearch,
                                   @Param("moneyFrom") Integer moneyFrom,
                                   @Param("moneyTo") Integer moneyTo,
                                   Pageable pageable);


    @Query("SELECT n FROM Note n WHERE n.customerId = :id AND n.createdBy IN :relatedUserList" )
    Page<Note> findNoteAll(@Param("id") Long id, @Param("relatedUserList") List<Long> relatedUserList, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.customerId = :id AND n.storeId = :storeID" )
    Page<Note> findNoteAll(@Param("id") Long id, @Param("storeID") Long storeID, Pageable pageable);
}
