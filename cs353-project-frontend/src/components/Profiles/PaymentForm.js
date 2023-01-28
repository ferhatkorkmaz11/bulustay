import React, { useEffect } from 'react';

import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import TextField from '@mui/material/TextField';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { FormGroup } from '@mui/material';
import Button from '@mui/material/Button';
import { AdapterDayjs } from '@mui/x-date-pickers-pro/AdapterDayjs';

export default function PaymentForm() {

    const [expDate, setExpDate] = React.useState('');
    const [name, setName] = React.useState('');
    const [cvv, setCvv] = React.useState('');
    const [cardNumber, setCardNumber] = React.useState('');
    const [balance, setBalance] = React.useState('');
    const [isLoading, setLoading] = React.useState(true)

    useEffect(() => {
        let requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };

        fetch("http://localhost:8080/myProfile?userId=" + localStorage.getItem("userId"), requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    let resultBody = resultJSON.body;
                    setBalance(resultBody.balance);
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));

    }, [isLoading]);

    const cancelInformation = () =>{
        setExpDate('');
        setName('');
        setCvv('');
        setCardNumber('');
        balance(0.0);
    }

    const saveInformation = ()=> {
        if (expDate === '' || name === '' || cvv === '' || cardNumber === '' ||balance=== '' || balance === '0.0'){
            alert("You have empty fields.");
        }
        else{

            var myHeaders = new Headers();
            myHeaders.append("Content-Type", "application/json");
          
          
            var raw = JSON.stringify({
              "CVV": cvv,
              "cardNo": cardNumber,
             "expirationDate": expDate,
              "cardOwner": name,
              "userId": parseInt(localStorage.getItem("userId")),
              "amount": balance 
            });
        

            var requestOptions = {
                method: 'POST',
                headers: myHeaders,
                body: raw,
                redirect: 'follow'
            };

            fetch("http://localhost:8080/addBalance", requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if(resultJSON.code === '200') {
                        let resultBody = resultJSON.body;
                        window.location.reload(false);
                        alert(resultJSON.message);
                    }
                    else{
                        alert(resultJSON.message);
                    }

                })
                .catch(error => console.log('error', error));
        }
    }
   

    return (
        <React.Fragment>
            <Typography variant="h6" gutterBottom>
                Payment method
            </Typography>
            <FormGroup row sx={{ paddingBottom: 2 }} >
                <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                        <TextField   onChange={event => setName(event.target.value)}
                            required
                            id="cardName"
                            label="Name on card"
                            fullWidth
                            autoComplete="cc-name"
                            variant="standard"
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <TextField   onChange={event => setCardNumber(event.target.value)}
                            required
                            id="cardNumber"
                            label="Card number"
                            type="number"
                            fullWidth
                            autoComplete="cc-number"
                            variant="standard"
                        />
                    </Grid>
                    <Grid item xs={12} md={6}>
                    <LocalizationProvider
                            dateAdapter={AdapterDayjs}
                            localeText={{ start: 'Start Date', end: 'End Date' }}
                        >
                            <DatePicker
                                views={['year', 'month']}
                                label="Expiry Date"
                                value={expDate}
                                onChange={(newValue) => {
                                    setExpDate(newValue);
                                }}
                                renderInput={(params) => <TextField {...params} helperText={null} />}
                            />
                        </LocalizationProvider>
                        
                    </Grid>
                    <Grid item xs={12} md={6}>
                        <TextField  onChange={event => setCvv (event.target.value)}
                            required
                            id="cvv"
                            label="CVV"
                            type="number"
                            helperText="Last three digits on signature strip"
                            fullWidth
                            autoComplete="cc-csc"
                            variant="standard"
                        />
                    </Grid>

                    <Grid item xs={12} md={6}>
                        <TextField  onChange={event => setBalance(event.target.value)}
                            required
                            id="balance"
                            label="Balance"
                            type="number"
                            fullWidth
                            autoComplete="balance"
                            variant="standard"
                        />
                    </Grid>
                    <Grid item xs={12}>

                    </Grid>
                </Grid>
            </FormGroup>
            <Button
                variant="contained" type='submit'
                onClick={() => {
                    saveInformation(); 
                  }}
                sx={{ mt: 4, background: "#92B4EC" }}
            >
                Submit
            </Button>    </React.Fragment>
    );
}
