import {
  faBackward,
  faCapsules,
  faCheckCircle,
  faEdit,
  faEyeSlash,
  faLeaf,
  faList,
  faSave,
  faTrash,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Badge,
  Button,
  Card,
  Center,
  Divider,
  Grid,
  Group,
  Menu,
  Radio,
  RadioGroup,
  Select,
  Text,
  TextInput,
  useMantineTheme,
} from "@mantine/core";
import { DatePicker } from "@mantine/dates";
import { useForm } from "@mantine/form";
import { useInputState } from "@mantine/hooks";
import { useModals } from "@mantine/modals";
import dayjs from "dayjs";
import customParseFormat from "dayjs/plugin/customParseFormat";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useMutation, useQueryClient } from "react-query";
import { useNavigate, useParams } from "react-router-dom";
import PatientDispensationList from "../../../components/dispensations/PatientDispensationList";
import { DISPENSATION_FLUX_EDIT_COLUMNS } from "../../../components/tables/columns/dispensation";
import CustomTable from "../../../components/tables/CustomTable";
import { EditableCell } from "../../../components/tables/EditableCell";
import { useUserContext } from "../../../hooks/context";
import { useFindFlux, useFluxMutation } from "../../../hooks/flux";
import {
  useFindFilteredOperation,
  useFindOperation,
  useOperationMutation,
} from "../../../hooks/operation";
import {
  useFindProduct,
  useGetRegimes,
  useProgramProducts,
} from "../../../hooks/product";
import { useFindPatient, useFindProvider } from "../../../hooks/shared";
import {
  Incidence,
  OperationStatus,
  QuantityType,
} from "../../../models/enums";
import {
  ProductOperationAttribute,
  ProductOperationAttributeSave,
  productOperationAttributeToSave,
  ProductOperationFlux,
  ProductOperationFluxSave,
  ProductOperationSave,
} from "../../../models/ProductOperation";
import OperationService from "../../../services/OperationService";
import { Fn } from "../../../utils/Fn";

dayjs.extend(customParseFormat);

const inventoryTypeUuid = "INVENTORYOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";
const operationTypeUuid = "DISPENSATIONOOOOOOOOOOOOOOOOOOOOOOOOOO";

const goalAttributeUuid = "GOALAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
const encounterAttributeUuid = "ENCOUNTERAAAAAAAAAAAAAAAAAAAAAAAAAAA";
const ageAttributeUuid = "PATIENTAGEAAAAAAAAAAAAAAAAAAAAAAAAAA";
const genderAttributeUuid = "PATIENTGENDERAAAAAAAAAAAAAAAAAAAAAAA";
const endDateAttributeUuid = "TREATMENTENDDATEAAAAAAAAAAAAAAAAAAAAAA";
const providerAttributeUuid = "PROVIDERAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
const regimeAttributeUuid = "REGIMENAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
const regimeLineAttributeUuid = "REGIMENLINEAAAAAAAAAAAAAAAAAAAAAAAAAAA";
const durationAttributeUuid = "TREATMENTDURATIONAAAAAAAAAAAAAAAAAAAAA";
const prescriptionDateAttributeUuid = "PRESCRIPTIONDATEAAAAAAAAAAAAAAAAAAAA";

type FluxForm = {
  product: string;
  quantity: string;
  relatedQuantity: string;
  uuid: string | undefined;
};

const DispensationFormPage = () => {
  const { type, dispensationId, identifier } = useParams();
  const { userLocation } = useUserContext();

  // console.log("identifier", identifier?.replaceAll(" ", "/"));

  const dispensationUuid = dispensationId ? dispensationId : "";
  const theme = useMantineTheme();

  const { operation: dispensation, getOperation: refetchDispensation } =
    useFindOperation(dispensationId ? dispensationId : "");

  const { operation: latestDispensation, getOperation: getLatestDispensation } =
    useFindFilteredOperation(
      operationTypeUuid,
      "last:validated",
      dispensation ? dispensation.productProgram.uuid : "",
      "",
      false
    );

  const patientIdentifier = identifier ? identifier.replaceAll(" ", "/") : "";

  const { patient } = useFindPatient(patientIdentifier, "&v=full");

  const { productRegimeSelectName } = useGetRegimes();

  productRegimeSelectName.push({ label: " AUCUN REGIME", value: "" });
  productRegimeSelectName.sort((a, b) => (a.label > b.label ? 1 : -1));

  const program = dispensation ? dispensation.productProgram.uuid : "";

  const navigate = useNavigate();
  const modals = useModals();
  const queryClient = useQueryClient();

  const [fluxUuid, setFluxUuid] = useState<string>("");
  const [productUuid, setProductUuid] = useInputState<string>("");

  const [patientAge, setPatientAge] = useInputState<string>("");
  const [patientGender, setPatientGender] = useInputState<string>("");
  const [dispensationRegime, setDispensationRegime] = useInputState<string>("");
  const [dispensationProvider, setDispensationProvider] =
    useInputState<string>("");
  const [dispensationGoal, setDispensationGoal] = useInputState<string>("");
  const [dispensationRegimeLine, setDispensationRegimeLine] =
    useInputState<string>("");
  const [dispensationDate, setDispensationDate] = useInputState<Date | null>(
    null
  );
  const [dispensationTreatmentEndDate, setDispensationTreatmentEndDate] =
    useInputState<string>("");
  const [dispensationPrescriptionDate, setDispensationPrescriptionDate] =
    useInputState<Date | null>(null);
  const [dispensationTreatmentDays, setDispensationTreatmentDays] =
    useInputState<string>("");

  //---> Previous dispensation info
  const [previousPatientAge, setPreviousPatientAge] = useInputState<
    number | null
  >(null);
  const [previousPatientGender, setPreviousPatientGender] =
    useInputState<string>("");
  const [previousDispensationRegime, setPreviousDispensationRegime] =
    useInputState<string>("");
  const [previousDispensationGoal, setPreviousDispensationGoal] =
    useInputState<string>("");
  const [previousDispensationRegimeLine, setPreviousDispensationRegimeLine] =
    useInputState<string>("");
  const [previousDispensationDate, setPreviousDispensationDate] =
    useInputState<Date | null>(null);
  const [
    previousDispensationTreatmentEndDate,
    setPreviousDispensationTreatmentEndDate,
  ] = useInputState<Date | null>(null);
  const [
    previousDispensationTreatmentDays,
    setPreviousDispensationTreatmentDays,
  ] = useInputState<number>(0);

  //---> Previous dispensation info

  const [quantityErrorMessage, setQuantityErrorMessage] =
    useInputState<string>("");
  const [maxQuantity, setMaxQuantity] = useInputState<number | null>(null);

  const { productSelectList, getProgramProducts } = useProgramProducts(
    dispensation ? dispensation.productProgram.uuid : "",
    "filter=regime" +
      (dispensationRegime && dispensationRegime !== ""
        ? `:${dispensationRegime}`
        : "")
  );

  const { flux, findFlux } = useFindFlux(
    fluxUuid,
    dispensationUuid,
    "dispensation"
  );

  const { product, getProduct } = useFindProduct(productUuid);

  const { providerSelectList } = useFindProvider();

  const {
    addFlux,
    removeFlux,
    updateFluxQuantity,
    updateFluxRelatedQuantity,
    updateFlux,
  } = useFluxMutation(dispensationUuid);

  const { updateOperationStatus, removeOperation } = useOperationMutation();

  const form = useForm<FluxForm>({
    initialValues: {
      product: "",
      quantity: "",
      relatedQuantity: "",
      uuid: "",
    },
    validate: (values) => ({
      product: !values.product || values.product === "" ? "Champ requis" : null,
      quantity:
        values.quantity === undefined || values.quantity.length === 0
          ? "Champ requis"
          : parseInt(values.quantity) === 0
          ? "Quantité > 0"
          : maxQuantity &&
            parseInt(values.quantity) > maxQuantity &&
            maxQuantity > 0
          ? "Quantité > "
          : null,
      relatedQuantity:
        values.relatedQuantity === undefined ||
        values.relatedQuantity.length === 0
          ? "Champ requis"
          : parseInt(values.relatedQuantity) === 0
          ? "Quantité > 0"
          : null,
    }),
  });

  const getAttributes = (): ProductOperationAttributeSave[] => {
    const attributes: ProductOperationAttribute[] =
      dispensation && dispensation.attributes ? dispensation.attributes : [];
    const operationAttributes: ProductOperationAttributeSave[] = [];

    if (patientAge !== "") {
      operationAttributes.push(
        createAttribute(patientAge, ageAttributeUuid, attributes)
      );
    }

    if (patientGender !== "") {
      operationAttributes.push(
        createAttribute(patientGender, genderAttributeUuid, attributes)
      );
    }

    if (dispensationTreatmentDays !== "") {
      operationAttributes.push(
        createAttribute(
          dispensationTreatmentDays,
          durationAttributeUuid,
          attributes
        )
      );
    }

    if (dispensationGoal !== "") {
      operationAttributes.push(
        createAttribute(dispensationGoal, goalAttributeUuid, attributes)
      );
    }

    if (dispensationPrescriptionDate) {
      operationAttributes.push(
        createAttribute(
          dispensationPrescriptionDate,
          prescriptionDateAttributeUuid,
          attributes
        )
      );
    }

    if (dispensationProvider !== "") {
      operationAttributes.push(
        createAttribute(dispensationProvider, providerAttributeUuid, attributes)
      );
    }

    if (dispensationTreatmentEndDate !== "") {
      operationAttributes.push(
        createAttribute(
          dispensationTreatmentEndDate,
          providerAttributeUuid,
          attributes
        )
      );
    }

    return operationAttributes;
  };

  const createAttribute = (
    value: any,
    uuid: string,
    attributes: ProductOperationAttribute[]
  ) => {
    const attribute = Fn.extractAttribute(attributes, uuid);
    if (attribute) {
      const a = productOperationAttributeToSave(attribute);
      a.value = value;
      return a;
    } else {
      return {
        operationAttributeType: ageAttributeUuid,
        value,
        location: userLocation.uuid,
      };
    }
  };
  const handleEditFluxQuantityInLine = useCallback(
    (id: string, value: number) => {
      setQuantityErrorMessage("");
      if (value.toString().length === 0) {
        setQuantityErrorMessage("Quantité requise");
      } else if ((maxQuantity && value > maxQuantity) || value === 0) {
        setQuantityErrorMessage("Quantité entre [1 .. " + maxQuantity + " ]");
        console.log(quantityErrorMessage);
      } else {
        updateFluxQuantity([value, id], {
          onSuccess: () => {
            console.log("Flux updated");
          },
        });
      }
    },
    [
      maxQuantity,
      quantityErrorMessage,
      setQuantityErrorMessage,
      updateFluxQuantity,
    ]
  );

  useEffect(() => {
    if (!dispensation) {
      refetchDispensation();
    } else {
      setDispensationDate(dispensation.operationDate);
    }
    if (latestDispensation) {
      setPreviousDispensationDate(latestDispensation.operationDate);
      setPreviousDispensationGoal(
        Fn.extractOperationAttribute(latestDispensation, goalAttributeUuid)
          ?.value
      );
      setPreviousPatientAge(
        Fn.extractOperationAttribute(latestDispensation, ageAttributeUuid)
          ?.value
      );
      setPreviousPatientGender(
        Fn.extractOperationAttribute(latestDispensation, genderAttributeUuid)
          ?.value
      );
      setPreviousDispensationRegime(
        Fn.extractOperationAttribute(latestDispensation, regimeAttributeUuid)
          ?.value
      );
      setPreviousDispensationRegimeLine(
        Fn.extractOperationAttribute(
          latestDispensation,
          regimeLineAttributeUuid
        )?.value
      );

      setPreviousDispensationTreatmentDays(
        Fn.extractOperationAttribute(latestDispensation, durationAttributeUuid)
          ?.value
      );
      setPreviousDispensationTreatmentEndDate(
        Fn.extractOperationAttribute(latestDispensation, endDateAttributeUuid)
          ?.value
      );
    }

    if (productUuid !== "" && form.values.product !== productUuid) {
      form.values.product = productUuid;
      // console.log(productUuid);
      getProduct();
      // console.log("product", product);
    }
  }, [
    dispensation,
    refetchDispensation,
    latestDispensation,
    setPreviousDispensationDate,
    setPreviousDispensationGoal,
    setPreviousPatientAge,
    setPreviousPatientGender,
    setPreviousDispensationRegime,
    setPreviousDispensationRegimeLine,
    setPreviousDispensationTreatmentDays,
    setPreviousDispensationTreatmentEndDate,
    productUuid,
    getProduct,
    form.values,
    product,
  ]);

  // console.log(
  //   "productUuid",
  //   productUuid,
  //   "form.values.product",
  //   form.values.product,
  //   "produit",
  //   product
  // );

  const handleEditFluxRelatedQuantityInLine = useCallback(
    (id: string, value: number) => {
      updateFluxRelatedQuantity([value, id], {
        onSuccess: () => {
          console.log("Flux updated");
        },
      });
    },
    [updateFluxRelatedQuantity]
  );

  const columns = useMemo(() => {
    DISPENSATION_FLUX_EDIT_COLUMNS.splice(3, 2);
    DISPENSATION_FLUX_EDIT_COLUMNS.push(
      {
        Header: "Quantité demandée",
        accessor: (data: ProductOperationFlux) => data.relatedQuantity,
        width: 100,
        Cell: (data: any) => {
          return dispensation &&
            dispensation.operationStatus !== "VALIDATED" ? (
            <EditableCell
              value={data.row.values["Quantité demandée"]}
              column={{
                id: data.row.values["Uuid"],
                attribute: data.row.values["AttributeUuid"],
              }}
              updateData={handleEditFluxRelatedQuantityInLine}
            />
          ) : (
            <Text style={{ textAlign: "left" }} size={"sm"}>
              {data.row.values["Quantité demandée"]}
            </Text>
          );
        },
      },
      {
        Header: "Quantité dispensée",
        accessor: (data: ProductOperationFlux) => data.quantity,
        width: 100,
        Cell: (data: any) =>
          dispensation && dispensation.operationStatus !== "VALIDATED" ? (
            <EditableCell
              value={data.row.values["Quantité dispensée"]}
              column={{
                id: data.row.values["Uuid"],
                attribute: data.row.values["AttributeUuid"],
              }}
              updateData={handleEditFluxQuantityInLine}
              // rightSectionText={}
            />
          ) : (
            <Text style={{ textAlign: "center" }} size={"sm"}>
              {data.row.values["Quantité dispensée"]}
            </Text>
          ),
      }
    );
    return DISPENSATION_FLUX_EDIT_COLUMNS;
  }, [
    dispensation,
    handleEditFluxQuantityInLine,
    handleEditFluxRelatedQuantityInLine,
  ]);

  const { mutate: saveOperation } = useMutation(OperationService.save);

  const { mutate: updateOperation } = useMutation(
    async (data: any) => await OperationService.update(data, dispensationUuid)
  );

  const validateOperation = () => {
    const program = dispensation
      ? dispensation.productProgram.uuid
      : patient ||
        patientIdentifier.match(/^[0-9]{4}\/.{2}\/[0-9]{2}\/[0-9]{5}E?$/g)
      ? "PNLSARVIOPPPPPPPPPPPPPPPPPPPPPPPPPPPPP"
      : "";

    const location = userLocation.uuid;

    if (dispensationUuid === "") {
      const productOperation: ProductOperationSave = {
        operationDate: dayjs(dispensationDate).toDate(),
        operationNumber: patientIdentifier,
        productProgram: program,
        quantityType: QuantityType.DISPENSATION,
        operationStatus: OperationStatus.VALIDATED,
        incidence: Incidence.NEGATIVE,
        operationType: operationTypeUuid,
        location,
      };
      productOperation.attributes = getAttributes();

      // console.log("productOperation", productOperation);

      saveOperation(productOperation, {
        onSuccess: (operation) => {
          if (type === "HIV" || patient) {
            navigate(
              `/supply/dispensation/view/${patientIdentifier.replaceAll(
                "/",
                "%20"
              )}/VIH`
            );
          } else {
            navigate(`/supply/dispensation`);
          }
        },
      });
    } else {
      if (dispensation) {
        if (dispensation.attributes && dispensation.attributes.length === 0) {
          updateOperation(getAttributes(), {
            onSuccess: () => {
              updateOperationStatus(
                { status: "VALIDATED", uuid: dispensationUuid },
                {
                  onSuccess: () => {
                    if (type === "HIV") {
                      navigate(
                        "supply/dispensation/" +
                          patientIdentifier.replaceAll("/", "%20") +
                          "/" +
                          type
                      );
                    } else {
                      navigate(`/supply/dispensation`);
                    }
                  },
                }
              );
            },
          });
        }
      }
    }
  };

  const cancelOperation = () => {
    updateOperationStatus(
      { status: "CANCELED", uuid: dispensationUuid },
      {
        onSuccess: () => {
          navigate(`/supply/dispensation`);
        },
      }
    );
  };
  const removeCurrentOperation = () => {
    removeOperation(dispensationUuid, {
      onSuccess: () => {
        queryClient.invalidateQueries("dispensation");
        // navigate("/supply/dispensation");
      },
    });
  };

  const getTreatmentEndDate = () => {
    setDispensationTreatmentEndDate("");
    // console.log("OK", dispensationTreatmentDays);
    if (
      (dispensationDate || dispensation) &&
      dispensationTreatmentDays &&
      dispensationTreatmentDays !== ""
    ) {
      const takePreviousEndDate = previousDispensationTreatmentEndDate
        ? dayjs(previousDispensationTreatmentEndDate).isAfter(
            dayjs(dispensationDate)
          )
        : false;

      const endDate = takePreviousEndDate
        ? dayjs(previousDispensationTreatmentEndDate).add(
            parseInt(dispensationTreatmentDays)
          )
        : dispensation
        ? dayjs(dispensation.operationDate).add(
            parseInt(dispensationTreatmentDays),
            "days"
          )
        : dayjs(dispensationDate).add(
            parseInt(dispensationTreatmentDays),
            "days"
          );

      setDispensationTreatmentEndDate(
        endDate.format("DD/MM/YYYYY").split("+")[0]
      );
    }
  };

  const onProductSelected = () => {
    if (productUuid !== "") {
      getProduct();
      if (product) {
        const stocks = product.stock;
        const program = dispensation ? dispensation.productProgram.name : "";

        setMaxQuantity(Fn.getProductStock(stocks, program));
      }
    } else {
      setMaxQuantity(null);
    }
    // console.log(product, maxQuantity);
  };

  const getProductList = () => {
    getProgramProducts();
    console.log(productSelectList);
  };

  const fluxes: ProductOperationFlux[] = useMemo(
    () => (dispensation ? dispensation.fluxes : []),
    [dispensation]
  );

  const handleDeleteFlux = (value: string) => {
    removeFlux(value, {
      onSuccess: () => {
        refetchDispensation();
      },
    });
  };

  const openConfirmModal = (value: string) =>
    modals.openConfirmModal({
      title: "Confirmer la suppression",
      children: (
        <Text size="sm">
          Vous êtes sur le point de supprimer le produit, voulez vous confirmer
          ?
        </Text>
      ),
      labels: { confirm: "Supprimer", cancel: "Annuler" },
      onCancel: () => console.log("Cancel"),
      onConfirm: () => handleDeleteFlux(value),
    });

  const createFluxFromForm = (
    values: typeof form["values"]
  ): ProductOperationFluxSave => {
    return {
      product: values.product,
      quantity: parseInt(values.quantity),
      relatedQuantity: parseInt(values.relatedQuantity),
      relatedQuantityLabel: "Quantité demandée dispensation",
      location: dispensation?.location.uuid,
    };
  };

  const handleSubmit = (values: typeof form["values"]) => {
    if (form.validate() && product) {
      const flux = createFluxFromForm(values);

      if (!fluxUuid) {
        addFlux(flux, {
          onSuccess: () => {
            refetchDispensation();
            form.reset();
          },
        });
      } else {
        updateFlux(
          { flux, fluxUuid },
          {
            onSuccess: () => {
              form.reset();
              setFluxUuid("");
              refetchDispensation();
            },
          }
        );
      }
    }
  };

  const hiddenColumns = ["AttributeUuid", "Uuid"];

  const tableHooks = (hooks: any) => {
    hooks.visibleColumns.push((columns: any) => [
      ...columns,
      {
        id: "Menu",
        Header: "",
        with: 10,
        maxWidth: 10,
        Cell: (data: any) => (
          <div style={{ textAlign: "right" }}>
            <Menu>
              <Menu.Item
                icon={<FontAwesomeIcon icon={faTrash} />}
                onClick={() => openConfirmModal(data.row.values.Uuid)}
              >
                Supprimer
              </Menu.Item>
            </Menu>
          </div>
        ),
      },
    ]);
  };

  // console.log(dispensationDate, dispensationTreatmentDays);

  return (
    <>
      {/* {(type === "HIV" || patient !== undefined || type === "PREVENTION") && ( */}
      <Card
        style={{
          border: 1,
          borderStyle: "solid",
          borderColor: theme.colors.blue[1],
        }}
        mt={"xs"}
      >
        <Card.Section>
          <Group position="apart">
            <Group
              m={"md"}
              position="left"
              style={{ marginBottom: 5, marginTop: theme.spacing.sm }}
            >
              <FontAwesomeIcon
                icon={faCapsules}
                size={"2x"}
                color={theme.colors.blue[7]}
              />
              <Text
                size="xl"
                weight={500}
                transform={"uppercase"}
                color={theme.colors.blue[7]}
              >
                Saisie dispensation
              </Text>
              {patientIdentifier ? (
                <>
                  <Text size="xl" weight={"bold"} color={"green"}>
                    PATIENT : {patientIdentifier}
                  </Text>
                  {(type === "HIV" || type === "PREVENTION") && (
                    <Text
                      size="xl"
                      weight={"bold"}
                      color={patient ? "green" : "orange"}
                    >
                      {patient ? "" : "MOBILE"}
                    </Text>
                  )}
                </>
              ) : (
                ""
              )}
            </Group>
            <Button
              leftIcon={<FontAwesomeIcon icon={faBackward} />}
              onClick={() => navigate("/supply/dispensation")}
            >
              Retour
            </Button>
          </Group>
        </Card.Section>
        <Card.Section>
          <Divider my={"xs"} />
        </Card.Section>
        <Grid columns={16}>
          {(type === "HIV" ||
            patient !== undefined ||
            type === "PREVENTION") && (
            <>
              <Grid.Col span={3}>
                <Card
                  style={{
                    border: 1,
                    borderStyle: "solid",
                    borderColor: theme.colors.blue[3],
                    backgroundColor: theme.colors.blue[1],
                  }}
                  p={"xs"}
                >
                  <Group spacing={"xs"} style={{ height: "4vh" }}>
                    <Text size="sm" color={"gray"}>
                      Age :
                    </Text>
                    <Text size="md" color={"blue"} weight={"bold"}>
                      {patient ? (
                        patient.person.age + " ans"
                      ) : previousPatientAge ? (
                        previousPatientAge + " ans"
                      ) : (
                        <TextInput
                          size="sm"
                          value={patientAge}
                          onChange={setPatientAge}
                          placeholder="Age du patient"
                          style={{ width: 80 }}
                        />
                      )}
                    </Text>
                    <Text size="sm" color={"gray"}>
                      Genre :
                    </Text>
                    <Text size="md" color={"blue"} weight={"bold"}>
                      {patient ? (
                        patient.person.gender
                      ) : previousPatientGender ? (
                        previousPatientGender
                      ) : (
                        <RadioGroup
                          size="sm"
                          value={patientGender}
                          onChange={setPatientGender}
                        >
                          <Radio label={"M"} value={"M"} />
                          <Radio label={"F"} value={"F"} />
                        </RadioGroup>
                      )}
                    </Text>
                  </Group>
                </Card>
              </Grid.Col>
              {type === "HIV" && (
                <Grid.Col span={13}>
                  <Card
                    style={{
                      border: 1,
                      borderStyle: "solid",
                      borderColor: theme.colors.green[3],
                      backgroundColor: theme.colors.green[1],
                    }}
                    p={"xs"}
                  >
                    <Group>
                      {!latestDispensation ? (
                        <Center style={{ height: "4vh" }}>
                          <Text size="lg" color={theme.colors.red[6]}>
                            Ce patient est probablement à sa première
                            dispensation sur votre site
                          </Text>
                        </Center>
                      ) : (
                        <>
                          <Text
                            size="sm"
                            color={"gray"}
                            transform={"uppercase"}
                            weight={"bold"}
                          >
                            Information dernière dispensation :
                          </Text>
                          <Text size="sm" color={"gray"}>
                            Régime :
                          </Text>
                          <Text size="md" color={"blue"} weight={"bold"}>
                            {previousDispensationRegime}
                          </Text>
                          <Text size="sm" color={"gray"}>
                            Ligne :
                          </Text>
                          <Text size="sm" color={"blue"} weight={"bold"}>
                            {previousDispensationRegimeLine}
                          </Text>
                          <Text size="sm" color={"gray"}>
                            Date de dispensation :
                          </Text>
                          <Text size="sm" color={"blue"} weight={"bold"}>
                            {previousDispensationDate}
                          </Text>
                          <Text size="sm" color={"gray"}>
                            durée du traitement :
                          </Text>
                          <Text size="sm" color={"blue"} weight={"bold"}>
                            {previousDispensationTreatmentDays}
                          </Text>
                          <Text size="sm" color={"gray"}>
                            Date de fin de traitement :
                          </Text>
                          <Text size="sm" color={"blue"} weight={"bold"}>
                            {dayjs(previousDispensationTreatmentEndDate).format(
                              "DD/MM/YYYY"
                            )}
                          </Text>
                        </>
                      )}
                    </Group>
                  </Card>
                </Grid.Col>
              )}
            </>
          )}
        </Grid>
      </Card>

      <Grid columns={10}>
        {type === "HIV" && patientIdentifier !== "" && (
          <Grid.Col span={4}>
            <PatientDispensationList
              title="Historique des dispensations"
              identifier={patientIdentifier}
              operationSelected={dispensationId}
              validated={true}
            />
            <PatientDispensationList
              title="Dispensations en cours de saisie"
              identifier={patientIdentifier}
              operationSelected={dispensationId}
              validated={false}
            />
          </Grid.Col>
        )}

        <Grid.Col span={type === "HIV" ? 6 : 10}>
          {dispensation === undefined && <Text>Latest dispensation</Text>}
          {dispensation !== undefined && (
            <Card
              style={{
                border: 1,
                borderStyle: "solid",
                borderColor: theme.colors.blue[1],
              }}
              mt={"xs"}
            >
              <Card.Section>
                <Group
                  m={"xs"}
                  position="apart"
                  style={{ marginBottom: 5, marginTop: theme.spacing.sm }}
                >
                  <Group>
                    <FontAwesomeIcon
                      icon={faList}
                      size={"1x"}
                      color={theme.colors.blue[9]}
                    />
                    <Text
                      color={"blue"}
                      weight={"bold"}
                      transform={"uppercase"}
                    >
                      Formulaire de dispensation
                    </Text>
                  </Group>

                  <Group>
                    {dispensation.operationStatus === "VALIDATED" ? (
                      <Button
                        size="xs"
                        color={"green"}
                        leftIcon={<FontAwesomeIcon icon={faEyeSlash} />}
                        onClick={cancelOperation}
                      >
                        Annuler dispensation
                      </Button>
                    ) : (
                      <>
                        <Button
                          size="xs"
                          color={"green"}
                          leftIcon={<FontAwesomeIcon icon={faCheckCircle} />}
                          onClick={validateOperation}
                        >
                          Enregistrer
                        </Button>
                        <Button
                          size="xs"
                          color={"red"}
                          leftIcon={<FontAwesomeIcon icon={faTrash} />}
                          onClick={removeCurrentOperation}
                        >
                          Supprimer
                        </Button>
                      </>
                    )}
                  </Group>
                </Group>
              </Card.Section>
              <Card.Section>
                <Divider my={"xs"} color={theme.colors.blue[1]} />
              </Card.Section>

              <Card
                style={{
                  backgroundColor: theme.colors.blue[1],
                  border: 1,
                  borderColor: theme.colors.blue[7],
                  borderStyle: "solid",
                }}
              >
                <Grid columns={12}>
                  {(type === "HIV" || type === "PREVENTION") && (
                    <>
                      <Grid.Col span={3}>
                        <Select
                          disabled={
                            dispensation.operationStatus !== "NOT_COMPLETED"
                          }
                          searchable
                          clearable
                          label={"Régime dipsensé"}
                          data={productRegimeSelectName}
                          value={dispensationRegime}
                          onChange={setDispensationRegime}
                          onBlur={getProductList}
                          size={"sm"}
                        />
                      </Grid.Col>
                      <Grid.Col span={4}>
                        <RadioGroup
                          label={"Ligne thérapeutique"}
                          size={"sm"}
                          value={dispensationRegimeLine}
                          onChange={setDispensationRegimeLine}
                        >
                          <Radio
                            disabled={
                              dispensation.operationStatus !== "NOT_COMPLETED"
                            }
                            label={"Ligne 1"}
                            value={"1"}
                          />
                          <Radio
                            disabled={
                              dispensation.operationStatus !== "NOT_COMPLETED"
                            }
                            label={"Ligne 2"}
                            value={"2"}
                          />
                          <Radio
                            disabled={
                              dispensation.operationStatus !== "NOT_COMPLETED"
                            }
                            label={"Ligne 3"}
                            value={"3"}
                          />
                        </RadioGroup>
                      </Grid.Col>
                      <Grid.Col span={3}>
                        {type === "HIV" ? (
                          <RadioGroup
                            label={"But"}
                            size={"sm"}
                            value={dispensationGoal}
                            onChange={setDispensationGoal}
                          >
                            <Radio
                              disabled={
                                dispensation.operationStatus !== "NOT_COMPLETED"
                              }
                              label={"PEC"}
                              value={"PEC"}
                            />
                            <Radio
                              disabled={
                                dispensation.operationStatus !== "NOT_COMPLETED"
                              }
                              label={"PTME"}
                              value={"PTME"}
                            />
                            <Radio
                              disabled={
                                dispensation.operationStatus !== "NOT_COMPLETED"
                              }
                              label={"Autre"}
                              value={"Autre"}
                            />
                          </RadioGroup>
                        ) : (
                          <RadioGroup
                            label={"But"}
                            size={"sm"}
                            value={dispensationGoal}
                            onChange={setDispensationGoal}
                          >
                            <Radio
                              disabled={
                                dispensation.operationStatus !== "NOT_COMPLETED"
                              }
                              label={"AES"}
                              value={"AES"}
                            />
                            <Radio
                              disabled={
                                dispensation.operationStatus !== "NOT_COMPLETED"
                              }
                              label={"PREP"}
                              value={"PREP"}
                            />
                          </RadioGroup>
                        )}
                      </Grid.Col>
                      <Grid.Col span={4}>
                        <Select
                          disabled={
                            dispensation.operationStatus !== "NOT_COMPLETED"
                          }
                          label={"Prescripteur"}
                          data={providerSelectList}
                          value={dispensationProvider}
                          onChange={setDispensationProvider}
                        />
                      </Grid.Col>
                    </>
                  )}

                  <Grid.Col span={2}>
                    <DatePicker
                      disabled={
                        dispensation.operationStatus !== "NOT_COMPLETED"
                      }
                      label={"Date de prescription"}
                      locale="fr"
                      inputFormat="DD/MM/YYYY"
                      value={dispensationPrescriptionDate}
                      onChange={setDispensationPrescriptionDate}
                      maxDate={
                        dispensation
                          ? dayjs(dispensation.operationDate)
                              // .add(1, "days")
                              .toDate()
                          : dayjs(new Date()).toDate()
                      }
                      defaultValue={
                        dispensation
                          ? dayjs(dispensation.operationDate).toDate()
                          : undefined
                      }
                    />
                  </Grid.Col>

                  <Grid.Col span={2}>
                    {dispensation ? (
                      <TextInput
                        disabled={
                          dispensation.operationStatus !== "NOT_COMPLETED"
                        }
                        label={"Date de dispensation"}
                        readOnly
                        color={theme.colors.blue[9]}
                        // mt={4}
                        value={
                          dayjs(dispensation.operationDate)
                            .format("DD/MM/YYYYY")
                            .split("+")[0]
                        }
                      />
                    ) : (
                      <DatePicker
                        label={"Date de dispensation"}
                        locale="fr"
                        size="sm"
                        inputFormat="DD/MM/YYYY"
                        maxDate={dayjs(new Date()).toDate()}
                        minDate={
                          dispensationPrescriptionDate
                            ? dayjs(dispensationPrescriptionDate).toDate()
                            : undefined
                        }
                        value={dispensationDate}
                        onChange={setDispensationDate}
                      />
                    )}
                  </Grid.Col>
                  <Grid.Col span={2}>
                    <TextInput
                      disabled={
                        dispensation.operationStatus !== "NOT_COMPLETED"
                      }
                      label={"Durée du traitement"}
                      size="sm"
                      value={dispensationTreatmentDays}
                      onChange={setDispensationTreatmentDays}
                      onBlur={getTreatmentEndDate}
                    />
                  </Grid.Col>
                  <Grid.Col span={2}>
                    <TextInput
                      disabled={
                        dispensation.operationStatus !== "NOT_COMPLETED"
                      }
                      label={"Fin de traitement"}
                      readOnly
                      value={dispensationTreatmentEndDate}
                      onChange={setDispensationTreatmentEndDate}
                    />
                  </Grid.Col>
                </Grid>
              </Card>

              <Card.Section>
                <Divider my={"xs"} color={theme.colors.blue[1]} />
              </Card.Section>
              <form onSubmit={form.onSubmit((values) => handleSubmit(values))}>
                <CustomTable
                  data={fluxes}
                  columns={columns}
                  initialState={{ hiddenColumns }}
                  tableHooks={
                    dispensation.operationStatus !== "NOT_COMPLETED"
                      ? undefined
                      : tableHooks
                  }
                  form={
                    dispensation.operationStatus === "NOT_COMPLETED" ? (
                      <tr style={{ backgroundColor: "#eee" }}>
                        <td colSpan={3}>
                          <Select
                            // required
                            searchable
                            clearable
                            nothingFound="Aucun produit trouvé"
                            placeholder="Choix du produit"
                            maxDropdownHeight={280}
                            icon={<FontAwesomeIcon icon={faLeaf} />}
                            data={productSelectList}
                            // {...form.getInputProps("product")}
                            // onChange={(e) => console.log(e)}
                            value={productUuid}
                            onChange={setProductUuid}
                            onBlur={onProductSelected}
                          />
                        </td>
                        <td>
                          <TextInput
                            {...form.getInputProps("relatedQuantity")}
                          />
                        </td>
                        <td>
                          <TextInput
                            {...form.getInputProps("quantity")}
                            rightSectionWidth={70}
                            rightSection={
                              maxQuantity || maxQuantity === 0 ? (
                                <Badge
                                  variant="filled"
                                  radius={"xs"}
                                  color={maxQuantity === 0 ? "red" : undefined}
                                >
                                  {maxQuantity}
                                </Badge>
                              ) : null
                            }
                            style={{ width: 200 }}
                          />
                        </td>
                        <td style={{ width: 10, textAlign: "right" }}>
                          {!maxQuantity && maxQuantity !== 0 ? (
                            ""
                          ) : maxQuantity === 0 ? (
                            <Text
                              color={"red"}
                              weight={"bold"}
                              transform={"uppercase"}
                            >
                              Pas en stock
                            </Text>
                          ) : form.values.quantity !== "" ? (
                            maxQuantity >= parseInt(form.values.quantity) ? (
                              <Button
                                type="submit"
                                color={fluxUuid ? "green" : ""}
                              >
                                <FontAwesomeIcon
                                  icon={fluxUuid ? faEdit : faSave}
                                />
                              </Button>
                            ) : (
                              <Text
                                color={"red"}
                                weight={"bold"}
                                transform={"uppercase"}
                              >
                                Trop grand
                              </Text>
                            )
                          ) : (
                            ""
                          )}
                        </td>
                      </tr>
                    ) : undefined
                  }
                />
              </form>
            </Card>
          )}
        </Grid.Col>
        {/* <Grid.Col span={2}>Cancelled dispensations</Grid.Col> */}
      </Grid>
    </>
  );
};

export default DispensationFormPage;
