package projects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
    private List<String> operations = List.of("1) Add a project", "2) List projects", "3) Select a project");
    private Scanner scanner = new Scanner(System.in);
    private ProjectService projectService = new ProjectService();
    private Project curProject;

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
                    case 2:
                        listProjects();
                        break;
                    case 3:
                        selectProject();
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

        if (curProject == null) {
            System.out.println("\nYou are not working with a project.");
        } else {
            System.out.println("\nYou are working with project: " + curProject);
        }
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
        // Collect info from the user
        String projectName = getStringInput("Enter project name");
        BigDecimal estimatedHours = new BigDecimal(getStringInput("Enter estimated hours"));
        BigDecimal actualHours = new BigDecimal(getStringInput("Enter actual hours"));
        int difficulty = getIntInput("Enter difficulty");
        String notes = getStringInput("Enter project notes");

        // Created a Project object
        Project project = new Project();
        project.setProjectName(projectName);
        project.setEstimatedHours(estimatedHours);
        project.setActualHours(actualHours);
        project.setDifficulty(difficulty);
        project.setNotes(notes);

        // Add materials
        List<Material> materials = new ArrayList<>();
        boolean addMoreMaterials = true;
        while (addMoreMaterials) {
            String materialName = getStringInput("Enter material name (or press Enter to finish adding materials)");
            if (materialName == null || materialName.isBlank()) {
                addMoreMaterials = false;
            } else {
                Material material = new Material();
                material.setName(materialName);
                materials.add(material);
            }
        }
        project.setMaterials(materials);

     // Add steps
        List<Step> steps = new ArrayList<>();
        boolean addMoreSteps = true;
        int stepOrder = 1;
        while (addMoreSteps) {
            String stepText = getStringInput("Enter step text (or press Enter to finish adding steps)");
            if (stepText == null || stepText.isBlank()) {
                addMoreSteps = false;
            } else {
                Step step = new Step();
                step.setStepText(stepText);
                step.setStepOrder(stepOrder++);
                steps.add(step);
            }
        }
        project.setSteps(steps);
        
        // Add project using service
        try {
            projectService.addProject(project);
            System.out.println("Project added successfully!");
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listProjects() {
        // Created variable to hold a List of Projects 
        List<Project> projects = projectService.fetchAllProjects();

        System.out.println("\nProjects:");

        int projectNumber = 1; // Initialize the project number

        for (Project project : projects) {
            System.out.println("  " + projectNumber + ": " + project.getProjectName());
            projectNumber++; // Increment the project number
        }
    }

    private void selectProject() {
        listProjects();

        Integer projectId = getIntInput("Enter a project ID to select a project");

        curProject = null;

        try {
            curProject = projectService.fetchProjectById(projectId);

            System.out.println("\nYou are working with project: " + curProject);
        } catch (NoSuchElementException e) {
            System.out.println("Invalid project ID selected.");
        }
    }
}
