package org.openhds.webservice.resources.bioko;

import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.controller.service.FieldWorkerService;
import org.openhds.controller.service.IndividualService;
import org.openhds.controller.service.OutMigrationService;
import org.openhds.controller.service.VisitService;
import org.openhds.domain.model.*;
import org.openhds.domain.model.bioko.OutMigrationForm;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.errorhandling.constants.ErrorConstants;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.errorhandling.util.ErrorLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.Serializable;
import java.io.StringWriter;

@Controller
@RequestMapping("/outMigrationForm")
public class OutMigrationFormResource extends AbstractFormResource {

    @Autowired
    private OutMigrationService outMigrationService;

    @Autowired
    private IndividualService individualService;

    @Autowired
    private FieldWorkerService fieldWorkerService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private ErrorHandlingService errorService;

    @Autowired
    private CalendarAdapter adapter;

    private JAXBContext context = null;
    private Marshaller marshaller = null;


    @RequestMapping(method = RequestMethod.POST, produces = "application/xml", consumes = "application/xml")
    @Transactional
    public ResponseEntity<? extends Serializable> processForm(@RequestBody OutMigrationForm outMigrationForm) throws JAXBException {

        try {
            context = JAXBContext.newInstance(OutMigrationForm.class);
            marshaller = context.createMarshaller();
            marshaller.setAdapter(adapter);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create JAXB context and marshaller for OutMigrationFormResource");
        }

        OutMigration outMigration = new OutMigration();

        outMigration.setRecordedDate(outMigrationForm.getDateOfMigration());
        outMigration.setDestination(outMigrationForm.getNameOfDestination());
        outMigration.setReason(outMigrationForm.getReasonForOutMigration());

        FieldWorker fieldWorker = fieldWorkerService.findFieldWorkerById(outMigrationForm.getFieldWorkerExtId());
        if (null == fieldWorker) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_FIELD_WORKER_EXT_ID);
            String errorDataPayload = createDTOPayload(outMigrationForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, OutMigrationForm.class.getSimpleName(),
                    fieldWorker, ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        outMigration.setCollectedBy(fieldWorker);

        Individual individual = individualService.findIndivById(outMigrationForm.getIndividualExtId());
        if (null == individual) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_INDIVIDUAL_EXT_ID);
            String errorDataPayload = createDTOPayload(outMigrationForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, OutMigrationForm.class.getSimpleName(),
                    fieldWorker, ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        outMigration.setIndividual(individual);

        Visit visit = visitService.findVisitById(outMigrationForm.getVisitExtId());
        if (null == visit) {
            ConstraintViolations cv = new ConstraintViolations();
            cv.addViolations(ConstraintViolations.INVALID_VISIT_EXT_ID);
            String errorDataPayload = createDTOPayload(outMigrationForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, OutMigrationForm.class.getSimpleName(),
                    fieldWorker, ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }
        outMigration.setVisit(visit);

        try {
            outMigrationService.createOutMigration(outMigration);
        } catch (ConstraintViolations cv) {
            String errorDataPayload = createDTOPayload(outMigrationForm);
            ErrorLog error = ErrorLogUtil.generateErrorLog(ErrorConstants.UNASSIGNED, errorDataPayload, null, OutMigrationForm.class.getSimpleName(),
                    fieldWorker, ErrorConstants.UNRESOLVED_ERROR_STATUS, cv.getViolations());
            errorService.logError(error);
            return requestError(cv);
        }

        return new ResponseEntity<OutMigrationForm>(outMigrationForm, HttpStatus.CREATED);

    }

    private String createDTOPayload(OutMigrationForm outMigrationForm) throws JAXBException {
        StringWriter writer = new StringWriter();
        marshaller.marshal(outMigrationForm, writer);
        return writer.toString();
    }


}