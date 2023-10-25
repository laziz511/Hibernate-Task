import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class App {
    public static void main(String[] args) {
        // Create and save students, groups, and courses in a single transaction
        Transaction transaction1 = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction1 = session.beginTransaction();

            Student student1 = aStudent("Steve", "Jobs", "steve@uzum.com");
            Student student2 = aStudent("Bob", "Gerry", "bob@uzum.com");
            Student student3 = aStudent("Tom", "Wine", "tom@uzum.com");

            Group group1 = aGroup("first group", List.of(student1, student2), 2026);
            Group group2 = aGroup("second group", List.of(student3), 2025);

            Course course1 = aCourse("Software Development", "This course is about software development", List.of(group1, group2));

            // Save the entities
            session.save(student1);
            session.save(student2);
            session.save(student3);
            session.save(group1);
            session.save(group2);
            session.save(course1);

            // Commit the transaction
            transaction1.commit();
        } catch (Exception e) {
            if (transaction1 != null) {
                transaction1.rollback();
            }
            e.printStackTrace();
        }

        // Now, in a separate session and transaction, retrieve and print students for a specific course
        Transaction transaction2 = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction2 = session.beginTransaction();

            String courseName = "Software Development";

            Course course = getCourseByName(session, courseName);

            List<Student> studentsForCourse = course.getStudentsForCourse(courseName);

            System.out.println("Students in Course '" + courseName + "':");
            studentsForCourse.forEach(System.out::println);

            transaction2.commit();
        } catch (Exception e) {
            if (transaction2 != null) {
                transaction2.rollback();
            }
            e.printStackTrace();
        }

    }

    // Helper method to retrieve a course by name
    private static Course getCourseByName(Session session, String courseName) {
        Query<Course> query = session.createQuery("FROM Course c WHERE c.courseName = :courseName", Course.class);
        query.setParameter("courseName", courseName);
        return query.uniqueResult();
    }

    // Helper methods to create entities
    private static Student aStudent(String firstName, String lastName, String email) {
        return new Student(firstName, lastName, email);
    }

    private static Group aGroup(String groupName, List<Student> students, int yearOfGraduation) {
        return new Group(groupName, students, yearOfGraduation);
    }

    private static Course aCourse(String courseName, String courseDescription, List<Group> groups) {
        return new Course(courseName, courseDescription, groups);
    }
}
