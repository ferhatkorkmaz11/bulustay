import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import "./Login.css";
import { useParams, withRouter, useHistory } from 'react-router-dom';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControl from '@mui/material/FormControl';
import FormLabel from '@mui/material/FormLabel';
import { useState } from "react";

function Copyright(props) {

  return (
    <Typography variant="body2" color="text.secondary" align="center" {...props}>
      {'Copyright © '}
      <Link color="inherit" href="">
        Buluştay
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}

    </Typography>
  );
}


const theme = createTheme();

export default function SignIn() {

  const handleSubmit = (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);



    let myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");

    let raw = JSON.stringify({
      "email": data.get('email'),
      "password": data.get('password')
    });

    let requestOptions = {
      method: 'POST',
      headers: myHeaders,
      body: raw,
      redirect: 'follow'
    };

    fetch("http://localhost:8080/login", requestOptions)
      .then(response => response.text())
      .then(result => {

        let resultJson = JSON.parse(result);
        if (resultJson.code === '200') {
          localStorage.setItem("userId", resultJson.body.userId);
          localStorage.setItem("email", resultJson.body.email);
          if(resultJson.body.isAdmin){
            history.push("/adminpanel")
          } else {
            history.push("/events");
          }
   
        } else {
          alert(resultJson.message);
        }

      })

      .catch(error => {alert("Something in the way...");console.log(error)});

  }

  const history = useHistory();
  

  return (
    <ThemeProvider theme={theme}>
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <div style={{ height: 250 }}>
          </div>
          <Typography component="h1" variant="h5">
            Welcome to Buluştay
          </Typography>
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email"
              name="email"
              autoComplete="email"
              autoFocus
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
            />

            <Button
              //href = "/home"
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2, background: "#30475E" }}
            >
              Log In
            </Button>
            <FormControl>


            </FormControl>
            <Button
              //href = "/home"
              fullWidth
              variant="contained"
              onClick={() => { history.push("/register") }}
              sx={{ mt: 3, mb: 2, background: "#30475E" }}
            >
              Register
            </Button>
          </Box>
        </Box>
        <Copyright sx={{ mt: 8, mb: 4 }} />
      </Container>
    </ThemeProvider>
  );
}