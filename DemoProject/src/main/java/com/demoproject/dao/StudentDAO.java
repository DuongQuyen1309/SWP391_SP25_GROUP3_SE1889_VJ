package com.demoproject.dao;

import com.demoproject.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class StudentDAO {

    private static EntityManagerFactory emf ;
    private static EntityManager em ;

    public StudentDAO(String persistenceName) {emf= Persistence.createEntityManagerFactory( persistenceName );}

    public void save(Student student) {
        try {
            em=emf.createEntityManager();
            em.getTransaction().begin();
            em.merge(student);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Error "+ e.getMessage());
        }

    }

    public List<Student> getStudents() {
        List<Student> students=null;
        try{
            em=emf.createEntityManager();
            em.getTransaction().begin();
            students=em.createQuery("from Student").getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Error "+ e.getMessage());
        }
        return students;
    }

    public Student find(Long id) {
        Student student=null;
        try{
            em=emf.createEntityManager();
            em.getTransaction().begin();
            student=em.find(Student.class, id);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Error "+ e.getMessage());
        }

        return student;
    }

    public void delete(Long id) {
        Student student=null;
        try{
            em=emf.createEntityManager();
            em.getTransaction().begin();
            student=em.find(Student.class, id);
            em.remove(student);
            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Error "+ e.getMessage());
        }
    }

    public void insertStudent(Student student) {
        try{
            em=emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(student);
            em.getTransaction().commit();

        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Error "+ e.getMessage());
        }
    }




}
