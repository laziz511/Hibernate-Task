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
            Course course2 = aCourse("Database Development", "This course is about Database development", List.of(group1));

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

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Retrieve students by course name
            String courseNameToSearch = "Software Development";
            List<Student> studentsInCourse = getStudentsByCourseName(session, courseNameToSearch);

            if (studentsInCourse != null) {
                System.out.println("Students in course " + courseNameToSearch + ":");
                for (Student student : studentsInCourse) {
                    System.out.println(student.getFirstName() + " " + student.getLastName());
                }
            } else {
                System.out.println("Course not found or no students in the course.");
            }
        }
    }

    // Helper method to retrieve students by course name
    private static List<Student> getStudentsByCourseName(Session session, String courseName) {
        Course course = getCourseByName(session, courseName);
        if (course != null) {
            return course.getGroups()
                    .stream()
                    .flatMap(group -> group.getStudents().stream())
                    .distinct()
                    .toList();
        }
        return null;
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

    private static Group aGroup(String groupName, List<Student> students, int yearOfgraduation) {
        return new Group(groupName, students, yearOfgraduation);
    }

    private static Course aCourse(String courseName, String courseDescription, List<Group> groups) {
        return new Course(courseName, courseDescription, groups);
    }
}
