package projects.dao;

import provided.util.DaoBase;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao extends DaoBase {
    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    public Project insertProject(Project project) {
        String sql = "INSERT INTO " + PROJECT_TABLE + " (project_name, estimated_hours, actual_hours, difficulty, notes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmnt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmnt.setString(1, project.getProjectName());
                stmnt.setBigDecimal(2, project.getEstimatedHours());
                stmnt.setBigDecimal(3, project.getActualHours());
                stmnt.setInt(4, project.getDifficulty());
                stmnt.setString(5, project.getNotes());

                int rowsAffected = stmnt.executeUpdate();

                if (rowsAffected == 0) {
                    rollbackTransaction(conn);
                    return null; // Insertion failed
                }

                // Retrieve the generated project_id
                try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setProjectId(generatedKeys.getInt(1));
                    } else {
                        rollbackTransaction(conn);
                        return null; // Failed to retrieve generated project_id
                    }
                }
            } catch (SQLException e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

            commitTransaction(conn);
            return project; // Project added successfully
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }


    public List<Project> fetchAllProjects() {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
        List<Project> projects = new ArrayList<>();

        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmnt = conn.prepareStatement(sql);
                 ResultSet rs = stmnt.executeQuery()) {

                while (rs.next()) {
                    Project project = extractProject(rs);
                    projects.add(project);
                }

            } catch (SQLException e) {
                rollbackTransaction(conn);
                System.err.println("Error executing SQL query: " + e.getMessage());
                throw new DbException(e);
            }

            commitTransaction(conn);
            return projects;
        } catch (SQLException e) {
            System.err.println("Error establishing database connection: " + e.getMessage());
            throw new DbException(e);
        }
    }

    private List<Material> fetchMaterials(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
        List<Material> materials = new ArrayList<>();

        try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
            stmnt.setInt(1, projectId);

            try (ResultSet rs = stmnt.executeQuery()) {
                while (rs.next()) {
                    Material material = extractMaterial(rs);
                    materials.add(material);
                }
            }
        }

        return materials;
    }

    private List<Step> fetchSteps(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
        List<Step> steps = new ArrayList<>();

        try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
            stmnt.setInt(1, projectId);

            try (ResultSet rs = stmnt.executeQuery()) {
                while (rs.next()) {
                    Step step = extractStep(rs);
                    steps.add(step);
                }
            }
        }

        return steps;
    }

    private List<Category> fetchCategories(Connection conn, Integer projectId) throws SQLException {
        String sql = "SELECT c.* FROM " + CATEGORY_TABLE + " c " +
                     "JOIN " + PROJECT_CATEGORY_TABLE + " pc ON c.category_id = pc.category_id " +
                     "WHERE pc.project_id = ?";
        List<Category> categories = new ArrayList<>();

        try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
            stmnt.setInt(1, projectId);

            try (ResultSet rs = stmnt.executeQuery()) {
                while (rs.next()) {
                    Category category = extractCategory(rs);
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
        return project;
    }

    private Material extractMaterial(ResultSet rs) throws SQLException {
        Material material = new Material();
        material.setMaterialId(rs.getInt("material_id"));
        material.setProjectId(rs.getInt("project_id"));
        material.setName(rs.getString("material_name"));
        return material;
    }

    private Step extractStep(ResultSet rs) throws SQLException {
        Step step = new Step();
        step.setStepId(rs.getInt("step_id"));
        step.setProjectId(rs.getInt("project_id"));
        step.setStepText(rs.getString("step_text"));
        step.setStepOrder(rs.getInt("step_order"));
        return step;
    }

    private Category extractCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        return category;
    }

    public boolean modifyProjectDetails(Project updatedProject) {
        String sql = "UPDATE " + PROJECT_TABLE + " SET project_name=?, estimated_hours=?, actual_hours=?, " +
                     "difficulty=?, notes=? WHERE project_id=?";
        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
                stmnt.setString(1, updatedProject.getProjectName());
                stmnt.setBigDecimal(2, updatedProject.getEstimatedHours());
                stmnt.setBigDecimal(3, updatedProject.getActualHours());
                stmnt.setInt(4, updatedProject.getDifficulty());
                stmnt.setString(5, updatedProject.getNotes());
                stmnt.setInt(6, updatedProject.getProjectId());

                int rowsAffected = stmnt.executeUpdate();

                if (rowsAffected == 0) {
                    rollbackTransaction(conn);
                    return false; // The project does not exist
                }
            } catch (SQLException e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

            commitTransaction(conn);
            return true; // Project updated successfully
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public boolean deleteProject(Integer projectId) {
        String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id=?";
        try (Connection conn = DbConnection.getConnection()) {
            startTransaction(conn);

            try (PreparedStatement stmnt = conn.prepareStatement(sql)) {
                stmnt.setInt(1, projectId);

                int rowsAffected = stmnt.executeUpdate();

                if (rowsAffected == 0) {
                    rollbackTransaction(conn);
                    System.out.println("Error: The project with ID " + projectId + " does not exist.");
                    return false; // The project does not exist
                }
            } catch (SQLException e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

            commitTransaction(conn);
            System.out.println("Project with ID " + projectId + " deleted successfully.");
            return true; // Project deleted successfully
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }
}
