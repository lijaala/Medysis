package Medysis.Project.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "Lab-Tests")
public class LabTests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int testID;

    @Column( name = "testName", nullable = false, unique = true)
    private String testName;

    @Column(name = "normalRange", nullable = false)
    private String normalRange;

    @Column(name = "measurementUnit")
    private String measurementUnit;

    public int getTestID() {
        return testID;
    }

    public void setTestID(int testID) {
        this.testID = testID;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getNormalRange() {
        return normalRange;
    }

    public void setNormalRange(String normalRange) {
        this.normalRange = normalRange;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }
}
