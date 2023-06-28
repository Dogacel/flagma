import * as React from "react";
import GlobalStyles from "@mui/joy/GlobalStyles";
import Avatar from "@mui/joy/Avatar";
import Divider from "@mui/joy/Divider";
import List from "@mui/joy/List";
import ListItem from "@mui/joy/ListItem";
import ListItemButton from "@mui/joy/ListItemButton";
import Sheet from "@mui/joy/Sheet";
import MuiLogo from "@/components/MuiLogo";
import { openSidebar } from "@/utils/utils";
import HomeIcon from "@mui/icons-material/Home";
import InsertChartIcon from "@mui/icons-material/InsertChart";
import LayersIcon from "@mui/icons-material/Layers";
import CheckBoxIcon from "@mui/icons-material/CheckBox";
import FlagIcon from "@mui/icons-material/Flag";
import GroupIcon from "@mui/icons-material/Group";
import SupportIcon from "@mui/icons-material/Support";
import SettingsIcon from "@mui/icons-material/Settings";

export default function FirstSidebar() {
  return (
    <Sheet
      className="FirstSidebar"
      variant="soft"
      color="primary"
      invertedColors
      sx={{
        position: {
          xs: "fixed",
          md: "sticky",
        },
        transform: {
          xs: "translateX(calc(100% * (var(--SideNavigation-slideIn, 0) - 1)))",
          md: "none",
        },
        transition: "transform 0.4s",
        zIndex: 10000,
        height: "100dvh",
        width: "var(--FirstSidebar-width)",
        top: 0,
        p: 1.5,
        py: 3,
        flexShrink: 0,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: 2,
        borderRight: "1px solid",
        borderColor: "divider",
      }}
    >
      <GlobalStyles
        styles={{
          ":root": {
            "--FirstSidebar-width": "68px",
          },
        }}
      />
      <MuiLogo />
      <List sx={{ "--ListItem-radius": "8px", "--List-gap": "12px" }}>
        <ListItem>
          <ListItemButton>
            <HomeIcon />
          </ListItemButton>
        </ListItem>
        <ListItem>
          <ListItemButton
            selected
            variant="solid"
            color="primary"
            onClick={() => openSidebar()}
          >
            <InsertChartIcon />
          </ListItemButton>
        </ListItem>
        <ListItem>
          <ListItemButton onClick={() => openSidebar()}>
            <LayersIcon />
          </ListItemButton>
        </ListItem>
        <ListItem>
          <ListItemButton onClick={() => openSidebar()}>
            <CheckBoxIcon />
          </ListItemButton>
        </ListItem>
        <ListItem>
          <ListItemButton onClick={() => openSidebar()}>
            <FlagIcon />
          </ListItemButton>
        </ListItem>
        <ListItem>
          <ListItemButton onClick={() => openSidebar()}>
            <GroupIcon />
          </ListItemButton>
        </ListItem>
      </List>
      <List
        sx={{
          mt: "auto",
          flexGrow: 0,
          "--ListItem-radius": "8px",
          "--List-gap": "8px",
        }}
      >
        <ListItem>
          <ListItemButton>
            <SupportIcon />
          </ListItemButton>
        </ListItem>
        <ListItem>
          <ListItemButton>
            <SettingsIcon />
          </ListItemButton>
        </ListItem>
      </List>
      <Divider />
      <Avatar variant="outlined" src="/static/images/avatar/3.jpg" />
    </Sheet>
  );
}
