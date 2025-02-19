package Medysis.Project.Controller;


import Medysis.Project.Model.Staff;
import Medysis.Project.Service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;
    @GetMapping("/all")
    public List<Staff> getAllStaff() {
        return staffService.getAllStaff();

    }
    @PutMapping("/edit/{staffID}")
    public ResponseEntity<Staff> updateStaffAvailability(
            @PathVariable String staffID,
            @RequestBody Staff updatedStaff) {
        try {
            Staff updated = staffService.updateStaffAvailability(staffID, updatedStaff);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
