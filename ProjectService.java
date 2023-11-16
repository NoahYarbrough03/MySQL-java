package projects.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import projects.dao.DbConnection;
import projects.dao.ProjectDao;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;

public class ProjectService {
    private ProjectDao projectDao = new ProjectDao();

    public Project addProject(Project project) {
        return projectDao.insertProject(project);
    }

    public List<Project> fetchAllProjects() {
        return projectDao.fetchAllProjects();
    }

    public Project fetchProjectById(Integer projectId) {
        String sql = "SELECT * FROM project WHERE project_id = ?";
        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try {
                Project project = null;

                try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
                    stmnt.setInt(1, projectId);

                    try (ResultSet rs = stmnt.executeQuery()) {
                        if (rs.next()) {
                            project = extractProject(rs);
                        }
                    }
                }

                if (project != null) {
                    project.setMaterials(fetchMaterials(conn, projectId));
                    project.setSteps(fetchSteps(conn, projectId));
                    project.setCategories(fetchCategories(conn, projectId));
                }

                commitTransaction(conn); // Commit the transaction if everything is successful
                return project; // Return the project here

            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public void modifyProjectDetails(Project updatedProject) {
        boolean success = projectDao.modifyProjectDetails(updatedProject);

        if (!success) {
            throw new DbException("The project does not exist.");
        }
    }

    public void deleteProject(Integer projectId) {
        boolean success = projectDao.deleteProject(projectId);

        if (!success) {
            throw new DbException("The project with ID " + projectId + " does not exist.");
        }
    }

    private void startTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    private void commitTransaction(Connection conn) throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    private void rollbackTransaction(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    private List<Material> fetchMaterials(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM material WHERE project_id = ?";
        List<Material> materials = new ArrayList<>();

        try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
            stmnt.setInt(1, projectId);

            try (ResultSet rs = stmnt.executeQuery()) {
                while (rs.next()) {
                    Material material = new Material();
                    material.setMaterialId(rs.getInt("material_id"));
                    material.setProjectId(rs.getInt("project_id"));
                    material.setName(rs.getString("material_name"));
          
                    materials.add(material);
                }
            }
        }

        return materials;
    }

    private List<Step> fetchSteps(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM step WHERE project_id = ?";
        List<Step> steps = new ArrayList<>();

        try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
            stmnt.setInt(1, projectId);

            try (ResultSet rs = stmnt.executeQuery()) {
                while (rs.next()) {
                    Step step = new Step();
                    step.setStepId(rs.getInt("step_id"));
                    step.setProjectId(rs.getInt("project_id"));
                    step.setStepText(rs.getString("step_text"));
                    step.setStepOrder(rs.getInt("step_order"));
                   
                    steps.add(step);
                }
            }
        }

        return steps;
    }

    private List<Category> fetchCategories(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT c.* FROM category c " +
                     "JOIN project_category pc ON c.category_id = pc.category_id " +
                     "WHERE pc.project_id = ?";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
            stmnt.setInt(1, projectId);

            try (ResultSet rs = stmnt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setCategoryId(rs.getInt("category_id"));
                    category.setCategoryName(rs.getString("category_name"));
              
                    categories.add(category);
                }
            }
        }

        return categories;
    }

    private Project extractProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setProjectId(rs.getInt("project_id"));
        project.setProjectName(rs.getString("project_name"));
        project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
        project.setActualHours(rs.getBigDecimal("actual_hours"));
        project.setDifficulty(rs.getInt("difficulty"));
        project.setNotes(rs.getString("notes"));
        // Add any additional properties you need to set
        return project;
    }
}
