package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.dao.FormRepository;
import com.github.cimsbioko.server.dao.FormSubmissionRepository;
import com.github.cimsbioko.server.domain.Form;
import com.github.cimsbioko.server.domain.FormId;
import com.github.cimsbioko.server.domain.FormSubmission;
import com.github.cimsbioko.server.exception.ExistingSubmissionException;
import com.github.cimsbioko.server.service.FormSubmissionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class FormSubmissionServiceImpl implements FormSubmissionService {

    private FormSubmissionRepository submissionDao;
    private FormRepository formDao;

    public FormSubmissionServiceImpl(FormSubmissionRepository submissionDao, FormRepository formDao) {
        this.submissionDao = submissionDao;
        this.formDao = formDao;
    }

    @Override
    @Transactional
    public FormSubmission recordSubmission(FormSubmission submission) throws ExistingSubmissionException {
        String instanceId = submission.getInstanceId();
        // FIXME: Use optional rather than null
        FormSubmission existing = submissionDao.findById(instanceId).orElse(null);
        if (existing != null) {
            throw new ExistingSubmissionException("submission with id " + instanceId + " exists", existing);
        } else {
            submissionDao.save(submission);
            // FIXME: Use optional rather than null
            Form form = formDao.findById(new FormId(submission.getFormId(), submission.getFormVersion())).orElse(null);
            form.setLastSubmission(Timestamp.from(Instant.now()));
            // FIXME: Use optional rather than null
            return submissionDao.findById(instanceId).orElse(null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormSubmission> getUnprocessed(int batchSize) {
        return submissionDao.findByProcessedNullOrderByCollected(PageRequest.of(0, batchSize));
    }

    @Override
    @Transactional
    public void markProcessed(FormSubmission submission, Boolean processedOk) {
        submission.setProcessedOk(processedOk);
        submission.setProcessed(Timestamp.from(Instant.now()));
        submissionDao.save(submission);
    }

}
