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
import "./RegisterStepTwo.css";
import { useParams, withRouter, useHistory } from 'react-router-dom';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import FormControl from '@mui/material/FormControl';
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker';
import FormLabel from '@mui/material/FormLabel';
import { useState } from "react";
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';

import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';


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
  const [birthday, setBirthday] = useState('');
  const [gender, setGender] =  useState('Female');

  const handleSubmit = (event) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);

  

    let myHeaders = new Headers();
  myHeaders.append("Content-Type", "application/json");
  let sqlstandartbirthday = birthday['$y'] + '-' + birthday['$M'] + '-' + birthday['$D'];
  console.log(sqlstandartbirthday);
  let raw = JSON.stringify({
    "email": localStorage.getItem("email"),
    "birthdate": sqlstandartbirthday,
    "city": data.get("city"),
    "district": data.get("district"),
    "neighbourhood": data.get('neighbourhood'),
    "streetNo": data.get('streetNo'),
    "street": data.get('street'),
    "building": data.get('building'),
    "phone": data.get('phone'),
    "gender": gender 
  });

  let requestOptions = {
    method: 'POST',
    headers: myHeaders,
    body: raw,
    redirect: 'follow'
  };

  fetch("http://localhost:8080/saveAdditionalRegisterInformation", requestOptions)
  .then(response => response.text())
  .then(result => {

    let resultJson = JSON.parse(result);
    if(resultJson.code === '200') {
      localStorage.setItem("userId", null);
      localStorage.setItem("email", null);
      history.push("/");
    }
    
  })

  .catch(error => alert('Something in the way..'));
    
  }

  const history =  useHistory();
 

  return (
    <ThemeProvider theme={theme}>
      <Container component="main" maxWidth="xs">
        <CssBaseline />
        <Box
          sx={{
            marginTop: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <div style={{height: 250}}>
          </div>
          <Typography component="h1" variant="h5">
            Welcome to Buluştay
          </Typography>
          <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
          <LocalizationProvider
           dateAdapter={AdapterDayjs}>
     
          <DesktopDatePicker
          label="Birthday"
          value={birthday}
          onChange={text => {setBirthday(text); console.log(text)}}
          inputFormat="MM/DD/YYYY"

          id='birthday'
          name='birthday'
        
          renderInput={(params) => <TextField fullWidth {...params} />}
        />
  
        </LocalizationProvider>
            <TextField
              margin="normal"
              required
              fullWidth
              id="phone"
              label="GSM"
              name="phone"
              autoComplete="phone"
              autoFocus
              type="tel"
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="city"
              label="City"
              name="city"
              autoComplete="city"
              autoFocus
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="district"
              label="District"
              name="district"
              autoComplete="district"
              autoFocus
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="neighbourhood"
              label="Neighbourhood"
              name="neighbourhood"
              autoComplete="neighbourhood"
              autoFocus
            />
            <TextField
              margin="normal"
              required
              fullWidth
              id="street"
              label="Street"
              name="street"
              autoComplete="street"
              autoFocus
            />
             <TextField
              margin="normal"
              required
              fullWidth
              id="streetNo"
              label="Street Number"
              name="streetNo"
              autoComplete="streetNo"
              autoFocus
            />
              <TextField
              margin="normal"
              required
              fullWidth
              id="building"
              label="Building"
              name="building"
              autoComplete="building"
              autoFocus
            />
           
            <InputLabel id="gender">Gender</InputLabel>
             <Select
               fullWidth
              labelId="gender"
              id="gender"
              value={gender}
              label="Gender"
              onChange={text => {setGender(text.target.value); console.log(text.target.value)}}
            >
          <MenuItem value={"Male"}>Male</MenuItem>
          <MenuItem value={"Female"}>Female</MenuItem>
          <MenuItem value={"Other"}>Other</MenuItem>

        </Select>
            
            
            <Button
              //href = "/home"
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2, background: "#30475E"}}
            >
                    Register
            </Button>
            <FormControl>
   
    </FormControl>
  
          </Box>
        </Box>
        <Copyright sx={{ mt: 8, mb: 4 }} />
      </Container>
    </ThemeProvider>
  );
}