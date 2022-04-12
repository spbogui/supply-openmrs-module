import { faClock } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Card,
  Group,
  TextInput,
  Divider,
  ScrollArea,
  useMantineTheme,
} from "@mantine/core";
import React from "react";
import PatientButton from "./PatientButton";

const ScheduledPatient = () => {
  const theme = useMantineTheme();

  return (
    <Card>
      <Card.Section>
        <Group
          m={"xs"}
          position="apart"
          style={{ marginBottom: 5, marginTop: theme.spacing.sm }}
        >
          <FontAwesomeIcon
            icon={faClock}
            size={"2x"}
            color={theme.colors.blue[9]}
          />
          <TextInput placeholder="Filtrer par numéro" style={{ width: 230 }} />
        </Group>
      </Card.Section>
      <Card.Section>
        <Divider mt={"xs"} />
      </Card.Section>
      <ScrollArea style={{ height: "60vh" }}>
        {/* <PatientButton
          identifier="1234/01/22/76879"
          age={30}
          treatmentEnDate={new Date()}
        />
        <PatientButton
          identifier="1234/01/22/76879"
          age={30}
          treatmentEnDate={new Date()}
          // onClick={() => console.log("Button clicked")}
          color={theme.colors.red[1]}
        /> */}
      </ScrollArea>
    </Card>
  );
};

export default ScheduledPatient;
