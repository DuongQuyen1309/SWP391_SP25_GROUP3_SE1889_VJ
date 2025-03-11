package com.demoproject.repository;

import com.demoproject.entity.ImportedNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportedNoteRepository extends JpaRepository<ImportedNote, Integer> {
}
