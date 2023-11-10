package projects.dao;

import provided.util.DaoBase;
import projects.entity.Project;
import projects.entity.Material;
import projects.entity.Step;
import projects.entity.Category;
import projects.exception.DbException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDao extends DaoBase {
    private static final String CATEGORY_TABLE = "category";
    private static final String MATERIAL_TABLE = "material";
    private static final String PROJECT_TABLE = "project";
    private static final String PROJECT_CATEGORY_TABLE = "project_category";
    private static final String STEP_TABLE = "step";

    // Existing insertProject method

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

            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }

            commitTransaction(conn);
            return projects;
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    public Project fetchProjectById(Integer projectId) {
        String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
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
                    commitTransaction(conn);
                    return project; // Return the project here
                } else {
                    // Handle the case where no project was found (e.g., throw an exception)
                    throw new DbException("Project not found with ID: " + projectId);
                }
            } catch (Exception e) {
                rollbackTransaction(conn);
                throw new DbException(e);
            }
        } catch (SQLException e) {
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

	public Project insertProject(Project project) {
		// TODO Auto-generated method stub
		return null;
	}
}
