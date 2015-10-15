package org.openmrs.module.bacteriology.api.specimen;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bacteriology.api.BacteriologyService;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterObservationServiceHelper;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.emrapi.encounter.mapper.ObsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpecimenMapper {

    @Autowired
    private ObsMapper obsMapper;

    @Autowired
    private ObsService obsService;

    @Autowired
    private ConceptService conceptService;

    public ObsMapper getObsMapper() {
        return obsMapper;
    }

    public void setObsMapper(ObsMapper obsMapper) {
        this.obsMapper = obsMapper;
    }

    public ObsService getObsService() {
        return obsService;
    }

    public void setObsService(ObsService obsService) {
        this.obsService = obsService;
    }

    public ConceptService getConceptService() {
        return conceptService;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    private void validate(org.openmrs.module.bacteriology.api.encounter.domain.Specimen etSpecimen) {

        if (etSpecimen.getType() == null)
            throw new IllegalArgumentException("Sample Type is mandatory");

        if (etSpecimen.getDateCollected() == null)
            throw new IllegalArgumentException("Sample Date Collected detail is mandatory");
    }



    public Specimen createSpecimen(Encounter encounter, org.openmrs.module.bacteriology.api.encounter.domain.Specimen etSpecimen) {
        validate(etSpecimen);

        Specimen bacteriologySpecimen = new Specimen();
        bacteriologySpecimen.setUuid(etSpecimen.getExistingObs());
        bacteriologySpecimen.setId(etSpecimen.getIdentifier());
        bacteriologySpecimen.setDateCollected(etSpecimen.getDateCollected());

        if (StringUtils.isNotEmpty(etSpecimen.getExistingObs())) {
            bacteriologySpecimen.setExistingObs(obsService.getObsByUuid(etSpecimen.getExistingObs()));
        }

        if (etSpecimen.getSample() != null && etSpecimen.getSample().getAdditionalAttributes() != null) {
            EncounterTransaction.Observation etObs = etSpecimen.getSample().getAdditionalAttributes();
            bacteriologySpecimen.setAdditionalAttributes(obsMapper.transformEtObs(bacteriologySpecimen.getExistingObs(), etObs));
        }

        bacteriologySpecimen.setType(getSampleTypeConcept(etSpecimen.getType()));

        if (etSpecimen.getReport() != null && etSpecimen.getReport().getResults() != null) {
            EncounterTransaction.Observation etObs = etSpecimen.getReport().getResults();
            bacteriologySpecimen.setReports(obsMapper.transformEtObs(bacteriologySpecimen.getReports(), etObs));
        }

        return bacteriologySpecimen;
    }


    private Concept getSampleTypeConcept(EncounterTransaction.Concept type) {
        Concept sampleType = conceptService.getConceptByUuid(type.getUuid());

        if (sampleType == null)
            throw new ConceptNotFoundException("Sample Type Concept " + type + " is not available");

        return sampleType;
    }

}
