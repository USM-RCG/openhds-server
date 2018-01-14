package com.github.cimsbioko.server.formproc.forms;

import com.github.cimsbioko.server.domain.Location;
import com.github.cimsbioko.server.exception.ConstraintViolations;
import com.github.cimsbioko.server.service.refactor.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Controller
public class SprayingFormProcessor extends AbstractFormProcessor {

    private static final Logger log = LoggerFactory.getLogger(SprayingFormProcessor.class);

    @Autowired
    private LocationService locationService;

    @Transactional
    public void processForm(Form form) {
        if (form.entityUuid != null) {
            Location location = locationService.getByUuid(form.entityUuid);
            if (location != null) {
                try {
                    switch (form.evaluation) {
                        case DESTROYED:
                            location.getAttrsForUpdate().put("status", "destroyed");
                            location.setDeleted(true);
                            break;
                        case UNINHABITED:
                            location.getAttrsForUpdate().put("status", "uninhabited");
                    }
                    locationService.save(location);
                } catch (ConstraintViolations cv) {
                    requestError(cv);
                }
            } else {
                log.info("location {} does not exist, ignoring", form.entityUuid);
            }
        }
    }

    @XmlEnum
    @XmlType
    public enum Evaluation {
        @XmlEnumValue("1")SPRAYED,
        @XmlEnumValue("2")REFUSED,
        @XmlEnumValue("3")CLOSED,
        @XmlEnumValue("4")UNINHABITED,
        @XmlEnumValue("5")DESTROYED
    }

    @XmlRootElement(name = "sprayingForm")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = Form.LOG_NAME)
    public static class Form implements Serializable {

        public static final String LOG_NAME = "SprayingForm";

        private String entityUuid;

        private Evaluation evaluation;
    }
}