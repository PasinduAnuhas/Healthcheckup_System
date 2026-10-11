package com.nutrihealth.service;

import com.nutrihealth.entity.ClinicReport;
import com.nutrihealth.repository.ClinicReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ClinicReportService {
    private final ClinicReportRepository repository;
    public ClinicReportService(ClinicReportRepository repository) { this.repository = repository; }
    public List<ClinicReport> findAll() { return repository.findAll(); }
    public ClinicReport findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }
    public ClinicReport save(ClinicReport report) { return repository.save(report); }
    public void delete(Long id) { repository.deleteById(id); }
}
