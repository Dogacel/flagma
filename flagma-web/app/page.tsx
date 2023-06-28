"use client";

import * as React from "react";
import { CssVarsProvider } from "@mui/joy/styles";
import CssBaseline from "@mui/joy/CssBaseline";
import Box from "@mui/joy/Box";
import Button from "@mui/joy/Button";
import Breadcrumbs from "@mui/joy/Breadcrumbs";
import Link from "@mui/joy/Link";
import Typography from "@mui/joy/Typography";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import FirstSidebar from "@/components/FirstSidebar";
import SecondSidebar from "@/components/SecondSidebar";
import OrderTable from "@/components/OrderTable";
import Header from "@/components/Header";
import ColorSchemeToggle from "@/components/ColorSchemeToggle";
import customTheme from "@/theme";
import HomeIcon from "@mui/icons-material/Home";
import CloudDownloadIcon from "@mui/icons-material/CloudDownload";
import TableViewIcon from "@mui/icons-material/TableView";

export default function JoyOrderDashboardTemplate() {
  return (
    <CssVarsProvider disableTransitionOnChange theme={customTheme}>
      <CssBaseline />
      <Box sx={{ display: "flex", minHeight: "100dvh" }}>
        <Header />
        <FirstSidebar />
        <SecondSidebar />
        <Box
          component="main"
          className="MainContent"
          sx={(theme) => ({
            px: {
              xs: 2,
              md: 6,
            },
            pt: {
              xs: `calc(${theme.spacing(2)} + var(--Header-height))`,
              sm: `calc(${theme.spacing(2)} + var(--Header-height))`,
              md: 3,
            },
            pb: {
              xs: 2,
              sm: 2,
              md: 3,
            },
            flex: 1,
            display: "flex",
            flexDirection: "column",
            minWidth: 0,
            height: "100dvh",
            gap: 1,
          })}
        >
          <Box sx={{ display: "flex", alignItems: "center" }}>
            <Breadcrumbs
              size="sm"
              aria-label="breadcrumbs"
              separator={<ChevronRightIcon />}
              sx={{
                "--Breadcrumbs-gap": "1rem",
                "--Icon-fontSize": "16px",
                fontWeight: "lg",
                color: "neutral.400",
                px: 0,
              }}
            >
              <Link
                underline="none"
                color="neutral"
                fontSize="inherit"
                href="#some-link"
                aria-label="Home"
              >
                <HomeIcon />
              </Link>
              <Link
                underline="hover"
                color="neutral"
                fontSize="inherit"
                href="#some-link"
              >
                Dashboard
              </Link>
              <Typography fontSize="inherit" variant="soft" color="primary">
                Orders
              </Typography>
            </Breadcrumbs>
            <ColorSchemeToggle
              sx={{ ml: "auto", display: { xs: "none", md: "inline-flex" } }}
            />
          </Box>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              my: 1,
              gap: 1,
              flexWrap: "wrap",
              "& > *": {
                minWidth: "clamp(0px, (500px - 100%) * 999, 100%)",
                flexGrow: 1,
              },
            }}
          >
            <Typography level="h1" fontSize="xl4">
              Orders
            </Typography>
            <Box sx={{ flex: 999 }} />
            <Box sx={{ display: "flex", gap: 1, "& > *": { flexGrow: 1 } }}>
              <Button
                variant="outlined"
                color="neutral"
                startDecorator={<CloudDownloadIcon />}
              >
                Download PDF
              </Button>
              <Button
                variant="outlined"
                color="neutral"
                startDecorator={<TableViewIcon />}
              >
                Download CSV
              </Button>
            </Box>
          </Box>
          <OrderTable />
        </Box>
      </Box>
    </CssVarsProvider>
  );
}
