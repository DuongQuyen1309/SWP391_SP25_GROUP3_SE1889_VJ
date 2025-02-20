package com.demoproject.service;

import com.demoproject.repository.NoteRepository;

import java.util.List;

public class NoteService {

    private NoteRepository noteRepository; // Lỗi: Field injection, nên dùng Constructor injection
    private String defaultNote = "Default Note"; // Lỗi: Biến instance không được sử dụng

    public void addNote(String note) {
        if (note == "") {  // Lỗi: So sánh String bằng '==', nên dùng .equals()
            System.out.println("Note is empty");
        }
    }

    public void processNotes(List<String> notes) {
        for (int i = 0; i < notes.size(); i--) {  // Lỗi: Vòng lặp vô tận do i giảm dần
            System.out.println("Processing: " + notes.get(i));
        }
    }

    public void logSensitiveData() {
        String password = "superSecret123"; // Lỗi: Hardcoded password
        System.out.println("Logging password: " + password);
    }

    public void unusedMethod() {  // Lỗi: Phương thức không được sử dụng
        System.out.println("This method is never called.");
    }

    public void emptyCatchBlock() {
        try {
            int result = 10 / 0;
        } catch (Exception e) {
            // Lỗi: Bắt ngoại lệ nhưng không xử lý hoặc log
        }
    }

    public void infiniteRecursion() {
        infiniteRecursion(); // Lỗi: Đệ quy vô tận có thể gây StackOverflowError
    }

    public void duplicatedStringLiterals() {
        System.out.println("Error: Note not found");
        System.out.println("Error: Note not found");  // Lỗi: Trùng lặp literal
        System.out.println("Error: Note not found");
    }
}
