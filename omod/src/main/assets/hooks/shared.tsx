import { SelectItem } from "@mantine/core";
import { useMutation, useQuery } from "react-query";
import SharedQuery from "../services/SharedQuery";
import { Fn } from "../utils/Fn";

export const useFindPatient = (
  identifier: string | undefined,
  params: string = "v=full"
) => {
  const {
    data,
    refetch: findPatient,
    isLoading,
  } = useQuery(
    ["patient", identifier, params],
    async () =>
      await SharedQuery.findPatient(identifier ? identifier : "", params),
    { enabled: identifier !== undefined }
  );

  const patient = data && data.length > 0 ? data[0] : undefined;
  return {
    patient,
    findPatient,
    isLoading,
  };
};

export const useFindProvider = (
  params: string = "v=full",
  enabled: boolean = true
) => {
  const {
    data,
    refetch: findProviders,
    isLoading,
  } = useQuery(
    ["patient", params],
    async () => await SharedQuery.findProviders(params),
    { enabled }
  );

  const providers = data ? data : [];
  const providerSelectList: SelectItem[] = data
    ? data.map((p: any) => {
        return { label: p.display, value: p.uuid };
      })
    : [];
  return {
    providers,
    findProviders,
    providerSelectList,
    isLoading,
  };
};

export const useFindEncounter = (
  uuid: string,
  params: string = "v=full",
  enabled: boolean = true
) => {
  const {
    data,
    refetch: findEncounter,
    isLoading,
  } = useQuery(
    ["encounter", uuid],
    async () => await SharedQuery.findEncounter(uuid, params),
    { enabled }
  );

  const encounter = data ? data : undefined;
  return {
    encounter,
    findEncounter,
    isLoading,
  };
};

export const useFindSettings = () => {
  const {
    data,
    isLoading,
    refetch: getSettings,
  } = useQuery(
    ["settings", "supply"],
    async () => await SharedQuery.findSupplySettings()
  );

  const dispensationDateConcept = data
    ? Fn.extractSettingValue(data, "DispensationDate") +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    : "";

  const dispensationGoalConcept = data
    ? Fn.extractSettingValue(data, "dispensationGoalConcept") +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    : "";

  const dispensationRegimenConcept = data
    ? Fn.extractSettingValue(data, "dispensationRegimenConcept") +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    : "";

  const dispensationRegimenLineConcept = data
    ? Fn.extractSettingValue(data, "dispensationRegimenLineConcept") +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    : "";

  const dispensationTreatmentDaysConcept = data
    ? Fn.extractSettingValue(data, "dispensationTreatmentDaysConcept") +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    : "";

  const dispensationTreatmentEndDateConcept = data
    ? Fn.extractSettingValue(data, "dispensationTreatmentEndDateConcept") +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    : "";
  const emergencyControlPointCenterAndNGOs = data
    ? Fn.extractSettingValue(data, "emergencyControlPointCenterAndNGOs")
    : "";

  const emergencyControlPointDirectClient = data
    ? Fn.extractSettingValue(data, "emergencyControlPointDirectClient")
    : "";

  const emergencyControlPointDistrict = data
    ? Fn.extractSettingValue(data, "emergencyControlPointDistrict")
    : "";

  const emergencyControlPointPointOfServiceDelivery = data
    ? Fn.extractSettingValue(
        data,
        "emergencyControlPointPointOfServiceDelivery"
      )
    : "";

  const monthsForCMM = data ? Fn.extractSettingValue(data, "monthsForCMM") : "";

  const stockMaxCenterAndOrganisations = data
    ? Fn.extractSettingValue(data, "stockMaxCenterAndOrganisations")
    : "";
  const stockMaxDirectClient = data
    ? Fn.extractSettingValue(data, "stockMaxDirectClient")
    : "";

  const stockMaxDistrict = data
    ? Fn.extractSettingValue(data, "stockMaxDistrict")
    : "";

  const stockMaxPointOfServiceDelivery = data
    ? Fn.extractSettingValue(data, "stockMaxDistrict")
    : "";

  return {
    dispensationDateConcept,
    dispensationGoalConcept,
    dispensationRegimenConcept,
    dispensationRegimenLineConcept,
    dispensationTreatmentDaysConcept,
    dispensationTreatmentEndDateConcept,
    emergencyControlPointCenterAndNGOs,
    emergencyControlPointDirectClient,
    emergencyControlPointDistrict,
    emergencyControlPointPointOfServiceDelivery,
    stockMaxCenterAndOrganisations,
    stockMaxDirectClient,
    stockMaxDistrict,
    stockMaxPointOfServiceDelivery,
    monthsForCMM,
    getSettings,
    isLoading,
  };
};

// Mutations

export const useMutateProvider = () => {
  const { mutate: saveProvider } = useMutation(async (data: any) => {
    return await SharedQuery.createProvider(data);
  });

  return {
    saveProvider,
  };
};

export const useMutatePerson = () => {
  const { mutate: savePerson } = useMutation(async (data: any) => {
    return await SharedQuery.createPerson(data);
  });

  return {
    savePerson,
  };
};
