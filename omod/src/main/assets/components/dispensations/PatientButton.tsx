import { faMale } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  UnstyledButton,
  Group,
  Avatar,
  Badge,
  useMantineTheme,
  Text,
} from "@mantine/core";
import dayjs from "dayjs";
import React from "react";
import { useNavigate } from "react-router-dom";

type PatientButtonProps = {
  identifier: string;
  age: number;
  treatmentEnDate: Date;
  color?: string;
  // onClick: () => void;
};

const PatientButton = (props: PatientButtonProps) => {
  const { identifier, age, treatmentEnDate, color } = props;
  const theme = useMantineTheme();
  const navigate = useNavigate();

  return (
    <UnstyledButton
      style={{
        border: 1,
        borderStyle: "solid",
        borderColor: color ? color : theme.colors.blue[4],
        borderRadius: 5,
        backgroundColor: color ? color : theme.colors.blue[1],
        width: "100%",
      }}
      onClick={() => navigate(`/supply/dispensation/view/${identifier}/VIH`)}
      p={"xs"}
      mt={"xs"}
    >
      <Group>
        <Avatar size={40} color="blue">
          <FontAwesomeIcon icon={faMale} size={"2x"} />
        </Avatar>
        <div>
          <Group position="apart">
            <Text size="xl" color={theme.colors.blue[9]} weight={"bold"}>
              {identifier}
            </Text>
            <Text size="xl" color={theme.colors.green[9]} weight={"bold"}>
              {age} ans
            </Text>
          </Group>
          <Group>
            <Text>Fin de traitement :</Text>
            <Text size="xl" color={theme.colors.green[9]} weight={"bold"}>
              {dayjs(treatmentEnDate).format("DD/MM/YYYY")}
            </Text>
            {/* <Badge size="xl" color={theme.colors.red[9]}>
              
            </Badge> */}
          </Group>
        </div>
      </Group>
    </UnstyledButton>
  );
};

export default PatientButton;
