/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.report.service;

import com.google.common.collect.Lists;
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.Project;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.persistence.project.dao.ProjectDao;
import net.rrm.ehour.persistence.user.dao.UserDao;
import net.rrm.ehour.project.service.ProjectService;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.report.criteria.UserSelectedCriteria;
import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.report.reports.element.ProjectStructuredReportElement;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Abstract report service provides utility methods for dealing
 * with the usercriteria obj
 */

public abstract class AbstractReportServiceImpl<RE extends ProjectStructuredReportElement> {
    @Autowired
    private UserDao userDAO;

    @Autowired
    private ProjectDao projectDAO;

    @Autowired
    private ProjectService projectService;

    /**
     * Get report data for criteria
     *
     * @param reportCriteria
     * @return
     */
    protected ReportData getReportData(ReportCriteria reportCriteria) {
        UserSelectedCriteria userSelectedCriteria = reportCriteria.getUserSelectedCriteria();

        DateRange reportRange = reportCriteria.getReportRange();

        List<RE> allReportElements  = generateReport(userSelectedCriteria, reportRange);

        if (userSelectedCriteria.isForPm()) {
            List<ProjectStructuredReportElement> elem = evictNonPmReportElements(userSelectedCriteria, allReportElements);

            return new ReportData(elem, reportRange);
        } else {
            return new ReportData(allReportElements, reportRange);
        }
    }

    private List<ProjectStructuredReportElement> evictNonPmReportElements(UserSelectedCriteria userSelectedCriteria, List<RE> allReportElements) {
        List<Integer> projectIds = fetchAllowedProjectIds(userSelectedCriteria);

        List<ProjectStructuredReportElement> allowedElements = Lists.newArrayList();

        for (ProjectStructuredReportElement reportElement : allReportElements) {
            if (projectIds.contains(reportElement.getProjectId())) {
                allowedElements.add(reportElement);
            }
        }
        return allowedElements;
    }

    private List<Integer> fetchAllowedProjectIds(UserSelectedCriteria userSelectedCriteria) {
        List<Project> allowedProjects = projectService.getProjectManagerProjects(userSelectedCriteria.getPm());

        List<Integer> projectIds = Lists.newArrayList();

        for (Project allowedProject : allowedProjects) {
            projectIds.add(allowedProject.getProjectId());
        }
        return projectIds;
    }


    private List<RE> generateReport(UserSelectedCriteria userSelectedCriteria, DateRange reportRange) {
        boolean noUserRestrictionProvided = userSelectedCriteria.isEmptyDepartments() && userSelectedCriteria.isEmptyUsers();
        boolean noProjectRestrictionProvided = userSelectedCriteria.isEmptyCustomers() && userSelectedCriteria.isEmptyProjects();

        List<Project> projects = null;
        List<User> users = null;

        if (!noProjectRestrictionProvided || !noUserRestrictionProvided) {
            if (noProjectRestrictionProvided) {
                users = getUsers(userSelectedCriteria);
            } else if (noUserRestrictionProvided) {
                projects = getProjects(userSelectedCriteria);
            } else {
                users = getUsers(userSelectedCriteria);
                projects = getProjects(userSelectedCriteria);
            }
        }

        return getReportElements(users, projects, reportRange);
    }

    /**
     * Get the actual data
     *
     * @param users
     * @param projects
     * @param reportRange
     * @return
     */
    protected abstract List<RE> getReportElements(List<User> users,
                                                  List<Project> projects,
                                                  DateRange reportRange);

    /**
     * Get project id's based on selected customers
     *
     * @param userSelectedCriteria
     * @return
     */
    protected List<Project> getProjects(UserSelectedCriteria userSelectedCriteria) {
        List<Project> projects;

        // No projects selected by the user, use any given customer limitation
        if (userSelectedCriteria.isEmptyProjects()) {
            if (!userSelectedCriteria.isEmptyCustomers()) {
                projects = projectDAO.findProjectForCustomers(userSelectedCriteria.getCustomers(),
                        userSelectedCriteria.isOnlyActiveProjects());
            } else {
                projects = null;
            }
        } else {
            projects = userSelectedCriteria.getProjects();
        }

        return projects;
    }

    /**
     * Get users based on selected departments
     *
     * @param userSelectedCriteria
     * @return
     */
    protected List<User> getUsers(UserSelectedCriteria userSelectedCriteria) {
        List<User> users;

        if (userSelectedCriteria.isEmptyUsers()) {
            if (!userSelectedCriteria.isEmptyDepartments()) {
                users = userDAO.findUsersForDepartments(userSelectedCriteria.getDepartments(), userSelectedCriteria.isOnlyActiveUsers());
            } else {
                users = null;
            }
        } else {
            users = userSelectedCriteria.getUsers();
        }

        return users;
    }


    /**
     * @param userDAO the userDAO to set
     */
    public void setUserDAO(UserDao userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * @param projectDAO the projectDAO to set
     */
    public void setProjectDAO(ProjectDao projectDAO) {
        this.projectDAO = projectDAO;
    }

    /**
     * @return the projectDAO
     */
    protected ProjectDao getProjectDAO() {
        return projectDAO;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
}
