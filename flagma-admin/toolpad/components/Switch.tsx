import * as React from "react";
import {
  Typography,
  Switch as MaterialSwitch,
  FormGroup,
  FormControlLabel,
} from "@mui/material";
import { createComponent } from "@mui/toolpad/browser";

export interface SwitchProps {
  value: boolean;
  label: string;
  onClick: () => void;
  onChange: (a: boolean) => void;
}

function Switch({ value, label, onClick, onChange }: SwitchProps) {
  const onClick2 = React.useCallback(() => {
    console.log({ onClick });
    onClick && onClick();
  }, [onClick]);

  const onChange2 = React.useCallback(() => {
    if (value !== undefined) {
      onChange(!value);
    }
  }, [value, onChange]);

  return (
    <FormGroup>
      <FormControlLabel
        control={
          <MaterialSwitch
            value={value}
            onClick={onClick2}
            onChange={onChange2}
          />
        }
        label={label}
      />
    </FormGroup>
  );
}

export default createComponent(Switch, {
  argTypes: {
    value: {
      type: "boolean",
      default: false,
      onChangeProp: "onChange",
    },
    label: {
      type: "string",
      default: "Hello world!",
    },
    onClick: {
      type: "event",
    },
    onChange: {
      type: "event",
    },
  },
});
