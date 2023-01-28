import React, { useEffect } from 'react';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Stack from '@mui/material/Stack';
import { useHistory } from 'react-router-dom';
import Navbar from "../Navbar/Navbar";
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import Button from '@mui/material/Button';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import PaymentFormDefault from './PaymentForm';
import { set } from 'date-fns';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import { styled } from '@mui/material/styles';


export default function MyProfile() {
    const Item = styled(Paper)(({ theme }) => ({
        backgroundColor: "#92B4EC",
        ...theme.typography.body2,
        textAlign: 'center',
        color: theme.palette.text.secondary,
      }));

    document.body.style = 'background: #F4F9F9;';

    const paymentForm = PaymentFormDefault();

    const [fName, setfName] = React.useState('');
    const [age, setAge] = React.useState('');
    const [city, setCity] = React.useState('');
    const [district, setDistrict] = React.useState('');
    const [neighbourhood, setNeighbourhood] = React.useState('');
    const [street, setStreet] = React.useState('');
    const [streetNo, setStreetNo] = React.useState('');
    const [building, setBuilding] = React.useState('');
    const [gender, setGender] = React.useState('');
    const [eventNumber, setEventNumber] = React.useState(0);


    const [phone, setPhone] = React.useState('');
    const [open, setOpen] = React.useState(false);
    const [balance, setBalance] = React.useState(0.0);
    const [followerNumber, setFollowerNumber] = React.useState(0);

    //BEN EKLEDİM BUNLARI FERHAT LA BEN
    const [email, setEmail] = React.useState("");
    //FERHAT EKLEME BİTİŞ
    const [isLoading, setLoading] = React.useState(true)

    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    fetch("http://localhost:8080/getFollowers?organizerId=" + localStorage.getItem("userId"), requestOptions)
        .then(response => response.text())
        .then(result => {
            let resultJSON = JSON.parse(result);
            if (resultJSON.code === '200') {
                setFollowerNumber(resultJSON.body.length);
            }
            else {
                alert(resultJSON.message)
            }
        })
        .catch(error => console.log('error', error));

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
                    setfName(resultBody.name);
                    setAge(resultBody.birthdate);
                    setCity(resultBody.city);
                    setDistrict(resultBody.district);
                    setNeighbourhood(resultBody.neighbourhood);
                    setStreet(resultBody.street);
                    setStreetNo(resultBody.streetNo);
                    setBuilding(resultBody.building);
                    setGender(resultBody.gender);
                    setBalance(resultBody.balance);
                    setPhone(resultBody.phone)
                    setEmail(resultBody.email);
                }
                else {
                    alert(resultJSON.message)
                }
            })
            .catch(error => console.log('error', error));

    }, [isLoading]);

    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    fetch("http://localhost:8080/getOrganizerEvents?organizerId=" + localStorage.getItem("userId") + "&userId=" + localStorage.getItem("userId"), requestOptions)
        .then(response => response.text())
        .then(result => {
            let resultJSON = JSON.parse(result);
            if (resultJSON.code === '200') {
                setEventNumber(resultJSON.body.length);
            }
            else {
                alert(resultJSON.message)
            }
        })
        .catch(error => console.log('error', error));

    const submitChanges = () => {
        let requestOptions = {
            method: 'PATCH',
            redirect: 'follow'
        };
        let query = "http://localhost:8080/editProfile?userId=" + localStorage.getItem("userId") + "&city=" + city + "&district=" + district + "&streetNo=" + streetNo + "&street=" + street + "&building=" + building + "&neighbourhood=" + neighbourhood + "&phone=" + phone;
        fetch(query, requestOptions)
            .then(response => response.text())
            .then(result => {
                let resultJSON = JSON.parse(result);
                if (resultJSON.code === '200') {
                    window.location.reload(false);
                }
            })
            .catch(error => console.log('error', error));

    };

    const handleGenderChange = (event) => {
        setGender(event.target.value);
    };

    const handleCityChange = (event) => {
        setCity(event.target.value);
    };

    const handleClickOpen = (position) => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };


    const history = useHistory();
    return (
        <>

            <Navbar></Navbar>
            <Stack direction="row" justifyContent="right">
            <Button color="primary"
                    variant="text"
                   // onClick={}
                   size='large'
                    sx={{ mt: 3, mr: 3}}
                >
                    Followers:  {followerNumber}
                </Button>
                <Button color="primary"
                    variant="text"
                   // onClick={}
                   size='large'
                    sx={{ mt: 3, mr: 3}}
                >
                    Created Events:  {eventNumber}
                </Button>
                <Button
                    variant="contained"
                    onClick={handleClickOpen}
                    sx={{ mt: 3, mr: 3, background: "#92B4EC" }}
                >
                    Add money to purse
                </Button>
            </Stack>
            <Dialog open={open} onClose={handleClose}  >
                <DialogContent>
                    <DialogContentText>
                        Your Current Balance: {balance}
                    </DialogContentText>

                    {paymentForm}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {
                        setOpen(false)
                    }} >Close </Button>

                </DialogActions>
            </Dialog>
            <Stack direction="column" sx={{ width: '80%', margin: '30px' }} justifyContent="center" >

                <React.Fragment>
                    <Typography variant="h6" gutterBottom>
                        My Profile
                    </Typography>

                    <Grid container spacing={3}>
                        <Grid item xs={12} >
                            <TextField
                                disabled
                                margin="normal"
                                required
                                fullWidth
                                id="email"
                                name="email"
                                value={email}
                                autoFocus
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setfName(event.target.value)}
                                disabled
                                id="firstName"
                                name="firstName"
                                label="First name"
                                fullWidth
                                value={fName}
                                autoComplete="given-name"
                                variant="standard"
                            />
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setAge(event.target.value)}
                                disabled
                                id="bdate"
                                name="bdate"
                                label="Birth Date"
                                fullWidth
                                value={age}
                                autoComplete="bdate"
                                variant="standard"
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setGender(event.target.value)}
                                disabled

                                id="gender"
                                name="gender"
                                label="Gender"
                                fullWidth
                                value={gender}
                                autoComplete="gender"
                                variant="standard"
                            />
                        </Grid>
                        <Grid item xs={12} >
                            <TextField
                                margin="normal"
                                fullWidth
                                id="phone"
                                label="GSM"
                                value={phone}
                                name="phone"
                                onChange={event => setPhone(event.target.value)}
                                autoComplete="phone"
                                autoFocus
                                type="tel"
                            />
                        </Grid>

                        <Grid item xs={12} sm={6}>

                            <FormControl sx={{ m: 1, minWidth: 150 }}>

                                <InputLabel id="dropdown-event-city" sx={{ display: 'inline-block' }}>City</InputLabel>
                                <Select
                                    labelId="dropdown-event-city"
                                    id="dropdown-city"
                                    value={city}
                                    onChange={handleCityChange}
                                    autoWidth
                                    label="City"
                                >
                                    <MenuItem value={"Ankara"}>Ankara</MenuItem>
                                    <MenuItem value={"Istanbul"}>Istanbul</MenuItem>
                                    <MenuItem value={"Konya"}>Konya</MenuItem>
                                    <MenuItem value={"Hatay"}>Hatay</MenuItem>
                                    <MenuItem value={"Adana"}>Adana</MenuItem>
                                    <MenuItem value={"Balikesir"}>Balikesir</MenuItem>

                                </Select>
                            </FormControl>

                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setDistrict(event.target.value)}
                                id="district"
                                name="district"
                                value={district}
                                label="District"
                                fullWidth
                                variant="standard"
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setNeighbourhood(event.target.value)}
                                id="neighbourhood"
                                name="neighbourhood"
                                value={neighbourhood}
                                label="Neighbourhood"
                                fullWidth
                                variant="standard"
                            />
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setStreet(event.target.value)}
                                id="street"
                                name="street"
                                value={street}
                                label="Street"
                                fullWidth
                                variant="standard"
                            />
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setStreetNo(event.target.value)}

                                id="streetNo"
                                name="streetNo"
                                value={streetNo}
                                label="Street No"
                                fullWidth
                                variant="standard"
                            />
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <TextField onChange={event => setBuilding(event.target.value)}
                                id="building"
                                name="building"
                                label="Building"
                                value={building}
                                fullWidth
                                autoComplete="building"
                                variant="standard"
                            />
                        </Grid>



                        <Button
                            variant="contained"
                            onClick={submitChanges}
                            sx={{ mt: 4, ml: 2, background: "#92B4EC" }}
                        >
                            Submit
                        </Button>
                    </Grid>
                </React.Fragment>
            </Stack>

        </>
    );
}