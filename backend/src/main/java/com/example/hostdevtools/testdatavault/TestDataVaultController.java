package com.example.hostdevtools.testdatavault;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-data-vault")
public class TestDataVaultController {

    private final TestDataVaultService service;

    public TestDataVaultController(TestDataVaultService service) {
        this.service = service;
    }

    @GetMapping("/processes")
    public List<String> listProcesses() {
        return service.listProcesses();
    }

    @PostMapping("/processes")
    public ResponseEntity<List<String>> registerProcess(@RequestBody ProcessRegisterRequest request) {
        try {
            return ResponseEntity.ok(service.registerProcess(request.getName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<TestDataEntry> list(@RequestParam(required = false) String process) {
        return service.listEntries(process);
    }

    @PostMapping
    public ResponseEntity<TestDataEntry> save(@RequestBody TestDataSaveRequest request) {
        return ResponseEntity.ok(service.save(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
