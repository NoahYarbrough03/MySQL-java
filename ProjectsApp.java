package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.service.ProjectService;
import projects.exception.DbException;

public class ProjectsApp {
    private List<String> operations = List.of("1) Add a project");
    private Scanner scanner = new Scanner(System.in);
    private ProjectService projectService = new ProjectService();

    public static void main(String[] args) {
        ProjectsApp app = new ProjectsApp();
        app.processUserSelections();
    }

    public void processUserSelections() {
        boolean done = false;

        while (!done) {
            try {
                int selection = getUserSelection();

                switch (selection) {
                    case 1:
                        addProject();
                        break;
                    case -1:
                        done = exitMenu();
                        break;
                    default:
                        System.out.println("\n" + selection + " is not a valid selection. Try again.");
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private int getUserSelection() {
        printOperations();
        Integer input = getIntInput("Enter a menu selection");

        if (Objects.isNull(input)) {
            return -1;
        }

        return input;
    }

    private void printOperations() {
        System.out.println("These are the available selections. Press enter to quit.");
        operations.forEach(operation -> System.out.println(operation));
    }

    private Integer getIntInput(String prompt) {
        String input = getStringInput(prompt);

        if (input == null) {
            return null;
        }

        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException(input + " is not a valid number. Try again.");
        }
    }

    private String getStringInput(String prompt) {
        System.out.print(prompt + ": ");
        String input = scanner.nextLine();

        if (input.isBlank()) {
            return null;
        }

        return input.trim();
    }

    private boolean exitMenu() {
        System.out.println("Exiting the menu.");
        return true;
    }

    public void addProject() {
        // Collect project details from the user
        String projectName = getStringInput("Enter project name");
        BigDecimal estimatedHours = new BigDecimal(getStringInput("Enter estimated hours"));
        BigDecimal actualHours = new BigDecimal(getStringInput("Enter actual hours"));
        int difficulty = getIntInput("Enter difficulty");
        String notes = getStringInput("Enter project notes");

        // Create a Project object
        Project project = new Project();
        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        // Add the project using the service
        try {
            projectService.addProject(project);
            System.out.println("Project added successfully!");
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
