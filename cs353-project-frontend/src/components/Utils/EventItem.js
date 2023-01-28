import React from "react";
import ListItem from '@mui/material/ListItem';
import Divider from '@mui/material/Divider';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Checkbox from '@mui/material/Checkbox';
import FavoriteBorder from '@mui/icons-material/FavoriteBorder';
import Favorite from '@mui/icons-material/Favorite';
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import Grid from '@mui/material/Grid';
import FormControl from '@mui/material/FormControl';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import PaymentFormDefault from '../Profiles/PaymentForm';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { FormGroup } from '@mui/material';
import { useParams, withRouter, useHistory } from 'react-router-dom';
import "./Item.css"

export default function EventItem(props) {
    const history = useHistory();
    const [expDate, setExpDate] = React.useState('');
    const [name, setName] = React.useState('');
    const [cvv, setCvv] = React.useState('');
    const [cardNumber, setCardNumber] = React.useState('');

    const [open2, setOpen2] = React.useState(false)
    const [payment, setPayment] = React.useState(0)

    const handlePaymentChange = (e) => {
        setPayment(e.target.value)
    }
    const handleClickOpen2 = (position) => {
        setOpen2(true)
    };
    const handleClose2 = () => {
        setOpen2(false)
    };

    const goToOrganizer = () => {
        if(parseInt(localStorage.getItem("userId")) === parseInt(props.organizerID) )
        {
            alert("You cannot go to your own page!")
        }
        else{
            history.push('/organizerprofile', { organizerID: props.organizerID })
        }
    }

    console.log(props)
    const enrollEvent = () => {
        let myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "userId": parseInt(localStorage.getItem("userId")),
            "eventId": props.eventID
        });

        console.log(props.eventID)

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/enroll", requestOptions)
            .then(response => response.text())
            .then(result => alert(JSON.parse(result).message))
            .catch(error => console.log('error', error));
    };

    const purchaseTicket = () => {
        if (payment === 1) {
            if (expDate === '' || name === '' || cvv === '' || cardNumber === '') {
                alert("You have empty fields.");
            }
            else{
                let myHeaders = new Headers();
                myHeaders.append("Content-Type", "application/json");
                var raw = JSON.stringify({
                    "CVV": cvv,
                    "cardNo": cardNumber,
                   "expirationDate": expDate,
                    "cardOwner": name,
                    "userId": parseInt(localStorage.getItem("userId")),
                    "amount": "" + props.ticketPrice
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
                              window.location.reload(true);
                          }
      
                      })
                      .catch(error => console.log('error', error));
                enrollEvent()
                handleClose2()
            }
        }
        else {
            enrollEvent()
            handleClose2()
        }
    }

    const handleFavorited = (event) => {
        if(props.isFavorited){
            var requestOptions = {
                method: 'DELETE',
                redirect: 'follow'
              };
              
              fetch("http://localhost:8080/removeFavorites?userId=" + localStorage.getItem("userId") + "&eventId=" + props.eventID, requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        window.location.reload(true);
                        alert(resultJSON.message)
                    }
                    else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));
        }
        else{
        let myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "userId": parseInt(localStorage.getItem("userId")),
            "eventId": parseInt(props.eventID)
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://localhost:8080/addFavorites", requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    alert(resultJSON.message)
                    window.location.reload(true);
           
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));
        }
    };

    return (
        <>
            <ListItem key={props.eventID} className="list-item" sx={{ width: '300px' }}>
                <Stack direction="column">
                    <ListItemText  
                        primary={props.name}
                        secondary={
                            <React.Fragment>
                                
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Event Location: {props.city}, {props.district}, {props.neighbourhood} {props.building}, {props.streetNo}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Start Date: {props.startDate}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"                                >
                                    Ticket Price: {props.ticketPrice}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Description: {props.description}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Quota: {props.quota}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Min Age: {props.minimumAge}
                                </Typography>
                                <br />
                                <Typography
                                    component="span"
                                    variant="body2"
                                >
                                    Event Type: {props.eventType}
                                </Typography>
                            </React.Fragment>
                        }
                    />
                    {props.ticketPrice ?
                        <Button
                            //href = "/home"
                            type="submit"
                            variant="contained"
                            onClick={handleClickOpen2}
                            sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                        >
                            Buy: {props.ticketPrice + "$"}
                        </Button>

                        :
                        <Button
                            //href = "/home"
                            type="submit"
                            variant="contained"
                            onClick={enrollEvent}
                            sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                        >
                            Enroll
                        </Button>
                    }
                    <Button
                            //href = "/home"
                            type="submit"
                            variant="contained"
                            onClick={goToOrganizer}
                            sx={{ mt: 3, mb: 2, background: "#92B4EC" }}
                        >
                            Organizer Profile
                    </Button>
                    <Checkbox icon={<FavoriteBorder />} checkedIcon={<Favorite />} checked={props.isFavorited} onChange={handleFavorited}
                        inputProps={{ 'aria-label': 'controlled' }} />
                    <Dialog open={open2} onClose={handleClose2}  >
                        <DialogTitle>Purchase</DialogTitle>
                        <DialogContent>
                            <DialogContentText>
                            </DialogContentText>
                            <Stack direction="column" sx={{ width: '80%', margin: '30px' }} justifyContent="center" >

                                <React.Fragment>

                                    <Grid container spacing={3}>

                                        <Grid item xs={12} sm={6}>
                                            <FormControl sx={{ m: 1, minWidth: 150 }}>
                                                <InputLabel id="dropdown-payment-type">Payment Type</InputLabel>
                                                <Select
                                                    labelId="dropdown-payment-type"
                                                    id="dropdown-payment"
                                                    value={payment}
                                                    onChange={handlePaymentChange}
                                                    label="Event Type"
                                                >
                                                    <MenuItem value={0}>From Balance</MenuItem>
                                                    <MenuItem value={1}>Credit Card</MenuItem>
                                                </Select>
                                            </FormControl>
                                        </Grid>
                                    </Grid>

                                    <Grid item xs={12} sm={6}>

                                        {payment ?
                                            <React.Fragment>
                                                <Typography variant="h6" gutterBottom>
                                                    Payment method
                                                </Typography>
                                                <FormGroup row sx={{ paddingBottom: 2 }} >
                                                    <Grid container spacing={3}>
                                                        <Grid item xs={12} md={6}>
                                                            <TextField onChange={event => setName(event.target.value)}
                                                                required
                                                                id="cardName"
                                                                label="Name on card"
                                                                fullWidth
                                                                autoComplete="cc-name"
                                                                variant="standard"
                                                            />
                                                        </Grid>
                                                        <Grid item xs={12} md={6}>
                                                            <TextField onChange={event => setCardNumber(event.target.value)}
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
                                                            <LocalizationProvider dateAdapter={AdapterDateFns}>
                                                                <DatePicker
                                                                    required
                                                                    label="Expiry Date"
                                                                    value={expDate}
                                                                    onChange={(newValue) => {
                                                                        setExpDate(newValue);
                                                                    }}
                                                                    renderInput={(params) => <TextField {...params} />}
                                                                />
                                                            </LocalizationProvider>
                                                        </Grid>
                                                        <Grid item xs={12} md={6}>
                                                            <TextField onChange={event => setCvv(event.target.value)}
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
                                                        <Grid item xs={12}>

                                                        </Grid>
                                                    </Grid>
                                                </FormGroup></React.Fragment>
                                            :
                                            <></>}

                                    </Grid>
                                </React.Fragment>
                            </Stack>
                        </DialogContent>
                        <DialogActions>
                            <Button onClick={handleClose2} >Cancel </Button>
                            <Button onClick={() => {
                                purchaseTicket();
                            }} >Purchase</Button>
                        </DialogActions>
                    </Dialog>
                </Stack>
            </ListItem>
            <Divider variant="inset" component="li" />
        </>
    )
}