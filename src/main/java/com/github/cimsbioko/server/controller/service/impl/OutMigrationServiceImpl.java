package com.github.cimsbioko.server.controller.service.impl;

import java.util.List;
import java.util.Set;

import com.github.cimsbioko.server.controller.service.*;
import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.OutMigration;
import com.github.cimsbioko.server.domain.model.Residency;
import com.github.cimsbioko.server.domain.util.CalendarUtil;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.service.SitePropertiesService;
import org.springframework.transaction.annotation.Transactional;

public class OutMigrationServiceImpl extends EntityServiceRefactoredImpl implements OutMigrationService {

    private ResidencyService residencyService;
    private IndividualService individualService;
    private MembershipService membershipService;
    private GenericDao genericDao;
    private SitePropertiesService siteProperties;

    public OutMigrationServiceImpl(ResidencyService residencyService, IndividualService individualService, MembershipService membershipService,
                                   GenericDao genericDao,
                                   SitePropertiesService siteProperties, EntityValidationService entityValidationService,
                                   CalendarUtil calendarUtil, CurrentUser currentUser) {
        super(genericDao, currentUser, calendarUtil, siteProperties, entityValidationService);
        this.residencyService = residencyService;
        this.individualService = individualService;
        this.membershipService = membershipService;
        this.genericDao = genericDao;
        this.siteProperties = siteProperties;

    }

    @Transactional(readOnly = true)
    public void evaluateOutMigrationBeforeCreate(OutMigration outMigration) throws ConstraintViolations {
        if (individualService.getLatestEvent(outMigration.getIndividual()).equals("Death")) {
            throw new ConstraintViolations(ConstraintViolations.ENTITY_REFERENCES_INDIVIDUAL_WITH_DEATH_EVENT);
        }

        // verify the individual has an open residency
        if (!residencyService.hasOpenResidency(outMigration.getIndividual())) {
            throw new ConstraintViolations(ConstraintViolations.ENTITY_REFERENCES_INDIVIDUAL_WITHOUT_OPEN_RESIDENCY);
        }

        Residency currentResidence = outMigration.getIndividual().getCurrentResidency();

        // verify the date of the out migration is after the residency start date
        if (currentResidence.getStartDate().compareTo(outMigration.getRecordedDate()) > 0) {
            throw new ConstraintViolations(ConstraintViolations.OUT_MIGRATION_BEFORE_INDIVIDUAL_RESIDENCY_START);
        }
    }

    public List<OutMigration> getOutMigrations(Individual individual) {
        return genericDao.findListByProperty(OutMigration.class, "individual", individual, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOutMigration(OutMigration outMigration) throws ConstraintViolations {

        if (null == outMigration) {
            throw new IllegalArgumentException("Cannot create a null out migration");
        }

        //evaluateOutMigrationBeforeCreate()
        evaluateOutMigrationBeforeCreate(outMigration);

        // configure out migration
        Residency currentResidence = outMigration.getIndividual().getCurrentResidency();
        outMigration.setResidency(currentResidence);
        currentResidence.setEndType(siteProperties.getOutmigrationCode());
        currentResidence.setEndDate(outMigration.getRecordedDate());

        residencyService.updateResidency(currentResidence);

        Set<Membership> memberships = outMigration.getIndividual().getAllMemberships();
        if (!memberships.isEmpty()) {
            for (Membership membership : memberships) {
                if (membership.getEndType().equals(siteProperties.getNotApplicableCode())) {
                    membership.setEndDate(outMigration.getRecordedDate());
                    membership.setEndType(siteProperties.getOutmigrationCode());
                    membershipService.updateMembership(membership);
                }
            }
        }

        create(outMigration);

    }
}
