import { useMutation, useQueries, useQuery } from "react-query";
import { ProductOperation } from "../models/ProductOperation";
import OperationService from "../services/OperationService";
import { OperationAttributeType, OperationType } from "../utils/constants";
import { Fn } from "../utils/Fn";
import { useFindEncounter, useFindSettings } from "./shared";

export const useFindFilteredOperation = (
  type: string,
  filter: string,
  program: string,
  params: string = "v=full",
  enabled: boolean = true
) => {
  const {
    data,
    refetch: getOperation,
    isLoading,
  } = useQuery(
    [type.replaceAll("O", "").toLowerCase(), `${filter}-${program}`],
    async () =>
      await OperationService.getAllOperations(
        type,
        `filter=${filter}&program=${program}&${params}`
      ),
    { enabled }
  );

  const operation = data && data.length > 0 ? data[0] : undefined;
  return {
    operation,
    getOperation,
    isLoading,
  };
};

export const useFindPatientDispensation = (identifier: string) => {
  const { operation: latestDispensation } = useFindFilteredDispensation(
    `operationNumber:${identifier}:validated:last`,
    "PNLSARVIOPPPPPPPPPPPPPPPPPPPPPPPPPPPPP",
    "v=full"
  );

  return {
    latestDispensation,
  };
};

export const useFindDispensation = (uuid: string) => {
  const { operation: dispensation, getOperation: getDispensation } =
    useFindOperation(uuid, "dispensations");

  const {
    dispensationGoalConcept,
    dispensationRegimenConcept,
    dispensationRegimenLineConcept,
    dispensationTreatmentDaysConcept,
    dispensationTreatmentEndDateConcept,
  } = useFindSettings();
  const operationNumber =
    dispensation && dispensation.operationNumber
      ? dispensation.operationNumber
      : "";
  const program = dispensation ? dispensation.productProgram.uuid : "";
  const { operation: latestDispensation } = useFindFilteredDispensation(
    `operationNumber:${operationNumber},validated`,
    program,
    "v=full"
  );

  const encounterUuid = latestDispensation
    ? Fn.extractOperationAttributeValue(
        latestDispensation,
        OperationAttributeType.ENCOUNTER
      )
    : undefined;
  // const encounterUuid = encounterAttribute ? encounterAttribute.value : "";

  const { encounter } = useFindEncounter(encounterUuid);

  const LatestRegime = encounter
    ? Fn.extractEncounterObsValue(encounter, dispensationRegimenConcept)
    : latestDispensation
    ? Fn.extractOperationAttributeValue(
        latestDispensation,
        OperationAttributeType.DISPENSATION_REGIME
      )
    : undefined;

  const latestGoal = encounter
    ? Fn.extractEncounterObsValue(encounter, dispensationGoalConcept)
    : latestDispensation
    ? Fn.extractOperationAttributeValue(
        latestDispensation,
        OperationAttributeType.DISPENSATION_GOAL
      )
    : undefined;
  const latestRegimeLine = encounter
    ? Fn.extractEncounterObsValue(encounter, dispensationRegimenLineConcept)
    : latestDispensation
    ? Fn.extractOperationAttributeValue(
        latestDispensation,
        OperationAttributeType.DISPENSATION_REGIME_LINE
      )
    : undefined;

  const latestTreatmentDays = encounter
    ? (Fn.extractEncounterObsValue(
        encounter,
        dispensationTreatmentDaysConcept
      ) as number)
    : latestDispensation
    ? Fn.extractOperationAttributeValue(
        latestDispensation,
        OperationAttributeType.DISPENSATION_DURATION
      )
    : undefined;

  const latestTreatmentEndDate = encounter
    ? (Fn.extractEncounterObsValue(
        encounter,
        dispensationTreatmentEndDateConcept
      ) as Date)
    : latestDispensation
    ? Fn.extractOperationAttributeValue(
        latestDispensation,
        OperationAttributeType.DISPENSATION_END_DATE
      )
    : undefined;

  // const latestTreatment = encounter
  //   ? (Fn.extractEncounterObsValue(
  //       encounter,
  //       "OperationAttributeType.ENCOUNTER"
  //     ) as Date)
  //   : latestDispensation
  //   ? Fn.extractOperationAttribute(
  //       latestDispensation,
  //       OperationAttributeType.DISPENSATION_REGIME
  //     )
  //   : undefined;

  return {
    dispensation,
    getDispensation,
    program,
    operationNumber,
    encounter,
    latestDispensation,
    latestGoal,
    latestRegimeLine,
    LatestRegime,
    latestTreatmentDays,
    latestTreatmentEndDate,
  };
};

export const useFindOperationType = (
  uuid: string,
  params: string = "v=full",
  enabled: boolean = true
) => {
  const { data, refetch: getOperationType } = useQuery(
    ["productOperationType", uuid],
    async () => await OperationService.getOperationType(uuid, params),
    { enabled }
  );

  const operationType = data ? data : undefined;

  return { operationType, getOperationType };
};

export const useFindOperation = (
  uuid: string,
  activity: string = "",
  enabled: boolean = true,
  params: string = "v=full"
) => {
  const {
    data,
    refetch: getOperation,
    isLoading,
  } = useQuery(
    [activity, "productOperation", uuid],
    async () => await OperationService.getOne(uuid, params),
    { enabled }
  );

  const operation = data ? data : undefined;

  return { operation, getOperation, isLoading };
};

export const useGetOperations = (type: string, params: string = "v=full") => {
  const {
    data,
    refetch: getOperations,
    isLoading,
  } = useQuery(
    [type.replaceAll("O", "").toLowerCase(), "all", params],
    async () => await OperationService.getAllOperations(type, params)
  );
  const operations = data ? data : [];
  return {
    operations,
    getOperations,
    isLoading,
  };
};

export const useGetTypesOperations = (
  types: string[],
  params: string = "v=full"
) => {
  const results = useQueries(
    types.map((type) => {
      return {
        queryKey: [[type.replaceAll("O", "").toLowerCase(), "all"]],
        queryFn: async () =>
          await OperationService.getAllOperations(type, params),
      };
    })
  );

  const isLoading = results.some((result) => result.isLoading);

  const data: ProductOperation[] = !isLoading
    ? results.reduce((acc: ProductOperation[], result) => {
        if (result.data) {
          acc.push(...result.data);
        }
        return acc;
      }, [])
    : [];

  const operations: ProductOperation[] = data;

  return { operations, isLoading };
};

export const useOperationMutation = () => {
  const { mutate: updateOperationStatus } = useMutation(
    async (data: { status: string; uuid: string }) => {
      return await OperationService.update(
        { operationStatus: data.status },
        data.uuid
      );
    }
  );

  const { mutate: saveOperation } = useMutation(OperationService.save);

  const { mutate: removeOperation } = useMutation(async (uuid: string) => {
    return await OperationService.remove(uuid);
  });

  return {
    updateOperationStatus,
    saveOperation,
    removeOperation,
  };
};

export const useFindFilteredDispensation = (
  filter: string,
  program: string,
  params: string = "v=full",
  enabled: boolean = true
) => {
  const { operation, getOperation, isLoading } = useFindFilteredOperation(
    OperationType.DISPENSATION,
    filter,
    program,
    params,
    enabled
  );

  return {
    operation,
    getOperation,
    isLoading,
  };
};
