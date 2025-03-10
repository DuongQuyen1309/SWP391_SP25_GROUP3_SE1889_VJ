package com.demoproject.service;

import com.demoproject.entity.Note;
import com.demoproject.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {
    @Autowired
    private final NoteRepository noteRepository;
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Page<Note> getNotes(Long id, List<Long> relatedUserList,Pageable pageable) {
        return noteRepository.findAllNote(id, relatedUserList, pageable);
    }
    public Page<Note> searchNoteByAttribute(Long id, List<Long> relatedUserList, Long idFrom, Long idTo, String kindOfNote, LocalDateTime createdDateFrom, LocalDateTime createdDateTo, String noteSearch,
                                            Integer moneyFrom, Integer moneyTo, Pageable pageable) { //lưu ý lại chỗ int và Integer vi dang o 2 doi tuong khac nhau
        return noteRepository.findNoteByAttribute(id, relatedUserList,idFrom, idTo,kindOfNote,createdDateFrom,createdDateTo,noteSearch,
                moneyFrom,moneyTo, pageable);
    }
    public Page<Note> searchNoteByAttribute(Long id, Long storeID, Long idFrom, Long idTo, String kindOfNote, LocalDateTime createdDateFrom, LocalDateTime createdDateTo, String noteSearch,
                                            Integer moneyFrom, Integer moneyTo, Pageable pageable) { //lưu ý lại chỗ int và Integer vi dang o 2 doi tuong khac nhau
        return noteRepository.findNoteByAttribute(id, storeID,idFrom, idTo,kindOfNote,createdDateFrom,createdDateTo,noteSearch,
                moneyFrom,moneyTo, pageable);
    }
    public Page<Note> searchNoteAll(Long id, List<Long> relatedUserList,  Pageable pageable) { //lưu ý lại chỗ int và Integer vi dang o 2 doi tuong khac nhau
        return noteRepository.findNoteAll(id, relatedUserList, pageable);
    }
    public Page<Note> searchNoteAll(Long id, Long storeID,  Pageable pageable) { //lưu ý lại chỗ int và Integer vi dang o 2 doi tuong khac nhau
        return noteRepository.findNoteAll(id, storeID, pageable);
    }
}
