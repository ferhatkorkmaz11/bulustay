import * as React from 'react';
import Navbar from "../Navbar/Navbar";
import Button from '@mui/material/Button';
import DialogTitle from '@mui/material/DialogTitle';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import TextField from '@mui/material/TextField';
import Stack from '@mui/material/Stack';
import Grid from '@mui/material/Grid';
import FormControl from '@mui/material/FormControl';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import Select from '@mui/material/Select';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import TextareaAutosize from '@mui/material/TextareaAutosize';
import FormHelperText from '@mui/material/FormHelperText';
import { useEffect } from "react";
import List from '@mui/material/List';
import OrgEventItem from "../Utils/OrgEventItem";
import { Typography } from "@mui/material";


export default function OrganizedEvents() {


    function createData(eventID, discountPercentage, refundPolicy, ticketPrice, streetNo, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate) {
        return { eventID, discountPercentage, refundPolicy, ticketPrice, streetNo, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate };
    }
    const [rows, setRows] = React.useState([])
    const [isLoading, setLoading] = React.useState(true);
    useEffect(() => {
        let requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };
        if (isLoading) {
            fetch("http://localhost:8080/getOrganizerEvents?organizerId=" + localStorage.getItem("userId") + "&userId=" + localStorage.getItem("userId"), requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;

                        for (let curEvent of curBody) {

                            rows.push(createData(curEvent.eventId, curEvent.discountPercentage, curEvent.refundPolicy, curEvent.ticketPrice, curEvent.streetNo, curEvent.description, curEvent.eventType, curEvent.city, curEvent.building, curEvent.neighbourhood, curEvent.street, curEvent.district, curEvent.quota, curEvent.minimumAge, curEvent.eventName, curEvent.startDate));

                            setRows(rows);
                            console.log(rows);
                        }
                        setLoading(false);

                    } else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));
        }

    }, [isLoading]);



    /**START OF INPUT VARIABLE DEFINITIONS **/
    const [eventCity, setEventCity] = React.useState('');
    const [eventName, setEventName] = React.useState('');
    const [eventType, setEventType] = React.useState('');
    const [eventCountry, setEventCountry] = React.useState('');
    const [eventDescription, setEventDescription] = React.useState('');
    const [eventDate, setEventDate] = React.useState(new Date());
    const [eventDistrict, setEventDistrict] = React.useState('');
    const [eventNeighbourhood, setEventNeighbourhood] = React.useState('');
    const [eventStreet, setEventStreet] = React.useState('');
    const [eventStreetNo, setEventStreetNo] = React.useState('');
    const [eventRefundPolicy, setEventRefundPolicy] = React.useState(true);


    const [eventBuilding, setEventBuilding] = React.useState('');

    const [minAge, setMinAge] = React.useState('');
    const [eventPrice, setEventPrice] = React.useState('');
    const [eventQuota, setEventQuota] = React.useState(null);

    const handleCityChange = (event) => {
        setEventCity(event.target.value);
    };

    const handleRefPoChange = (event) => {
        setEventRefundPolicy(event.target.value);
    };

    const handleEventTypeChange = (event) => {
        setEventType(event.target.value);
    };

    const handleMinAgeChange = (event) => {
        setMinAge(event.target.value);
    };


    const [open, setOpen] = React.useState(false);
    const handleClickOpen = (position) => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const cancelDialog = () => {
        setEventName('');
        setEventCity('');
        setEventDistrict('');
        setEventNeighbourhood('');
        setEventStreet('');
        setEventStreetNo('');
        setEventBuilding('');
        setEventDescription('');
        setEventDate('');
        setEventPrice('');
        setEventType('');
        setEventQuota('');
        setEventRefundPolicy(true);
        setMinAge('');
    }


    const createEvent = () => {
        // console.log(eventName, eventAddress, eventDate, eventQuota, eventPrice, eventDescription, eventCountry, eventCity, eventType, eventState, minAge);
        if (eventName === '' || eventType === '' || eventDate === '' || eventCity === '' || eventStreetNo === '' || eventStreet === '' || eventBuilding === '' || eventNeighbourhood === '' || eventDistrict === '' || eventQuota === '' || eventPrice === '' || eventDate === '' || minAge === '' || eventRefundPolicy === '') {
            alert("Please fill all the fields");
        }
        else {
            var myHeaders = new Headers();
            myHeaders.append("Content-Type", "application/json");
            eventDate.setSeconds(0)
            let tempDate = eventDate;
            tempDate.setHours(tempDate.getHours() + 3);
            let eventTimeReal = tempDate.toISOString().slice(0, 19).replace('T', ' ');

            var raw = JSON.stringify({
                "eventName": eventName,
                "city": eventCity,
                "district": eventDistrict,
                "neighbourhood": eventNeighbourhood,
                "streetNo": eventStreetNo,
                "street": eventStreet,
                "building": eventBuilding,
                "startDate": eventTimeReal,
                "description": eventDescription,
                "quota": parseInt(eventQuota),
                "minimumAge": parseInt(minAge),
                "eventType": eventType,
                "organizerId": parseInt(localStorage.getItem('userId')),
                "ticketPrice": eventPrice,
                "refundPolicy": eventRefundPolicy
            });

            var requestOptions = {
                method: 'POST',
                headers: myHeaders,
                body: raw,
                redirect: 'follow'
            };

            fetch("http://localhost:8080/createEvent", requestOptions)
                .then(response => response.text())
                .then(result => {
                    let responseJSON = JSON.parse(result);
                    if (responseJSON.code === '200') {
                        window.location.reload(true)
                        handleClose()
                        cancelDialog()
                        alert(responseJSON.message);
                    } else {
                        alert(responseJSON.message);
                    }
                })
                .catch(error => console.log('error', error));


        }
    }
    /**END OF INPUT VARIABLE DEFINITIONS **/

    return (
        <>
            <Navbar></Navbar>
            <Typography variant="h6" gutterBottom style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '20px' }}>Organized Events</Typography>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '10px' }}>
                <Stack direction="row" justifyContent="right">
                    <Button
                        variant="contained"
                        onClick={handleClickOpen}
                        sx={{ mt: 3, mr: 3, background: "#92B4EC" }}
                    >
                        Create an Event
                    </Button>
                </Stack>

                <Dialog open={open} onClose={handleClose}  >
                    <DialogTitle>Create an Event </DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            (If it is a free event, you should enter 0 as a price)
                        </DialogContentText>
                        <Stack direction="column" sx={{ width: '80%', margin: '30px' }} justifyContent="center" >

                            <React.Fragment>

                                <Grid container spacing={3}>
                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventName(event.target.value)}
                                            required
                                            id="eventName"
                                            name="eventName"
                                            label="Event name"
                                            fullWidth
                                            autoComplete="eventName"
                                            variant="standard"
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6}>

                                        <FormControl sx={{ m: 1, minWidth: 150 }}>
                                            <InputLabel id="dropdown-event-type">Event Type</InputLabel>
                                            <Select
                                                labelId="dropdown-event-type"
                                                id="dropdown-event"
                                                value={eventType}
                                                onChange={handleEventTypeChange}
                                                label="Event Type"
                                            >
                                                <MenuItem value={"Sport"}>Sport</MenuItem>
                                                <MenuItem value={"Music"}>Music</MenuItem>
                                                <MenuItem value={"Party"}>Party</MenuItem>
                                                <MenuItem value={"Art"}>Art</MenuItem>
                                                <MenuItem value={"Gathering"}>Gathering</MenuItem>
                                                <MenuItem value={"Movie"}>Movie</MenuItem>
                                                <MenuItem value={"Technology"}>Technology</MenuItem>

                                            </Select>
                                        </FormControl>

                                    </Grid>

                                    <Grid item xs={12} sm={6}>

                                        <FormControl sx={{ m: 1, minWidth: 150 }}>

                                            <InputLabel id="dropdown-event-city" sx={{ display: 'inline-block' }}>City</InputLabel>
                                            <Select
                                                labelId="dropdown-event-city"
                                                id="dropdown-city"
                                                value={eventCity}
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
                                        <TextField onChange={event => setEventDistrict(event.target.value)}
                                            required
                                            id="district"
                                            name="district"
                                            value={eventDistrict}
                                            label="District"
                                            fullWidth
                                            variant="standard"
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventNeighbourhood(event.target.value)}
                                            required
                                            id="neighbourhood"
                                            name="neighbourhood"
                                            value={eventNeighbourhood}
                                            label="Neighbourhood"
                                            fullWidth
                                            variant="standard"
                                        />
                                    </Grid>

                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventStreet(event.target.value)}
                                            required
                                            id="street"
                                            name="street"
                                            value={eventStreet}
                                            label="Street"
                                            fullWidth
                                            variant="standard"
                                        />
                                    </Grid>

                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventStreetNo(event.target.value)}
                                            required
                                            id="streetNo"
                                            name="streetNo"
                                            value={eventStreetNo}
                                            label="Street No"
                                            fullWidth
                                            variant="standard"
                                        />
                                    </Grid>

                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventBuilding(event.target.value)}
                                            required
                                            id="building"
                                            name="building"
                                            label="Building"
                                            fullWidth
                                            autoComplete="building"
                                            variant="standard"
                                        />
                                    </Grid>
                                    <Grid item xs={12} md={6}>
                                        <LocalizationProvider dateAdapter={AdapterDateFns}>
                                            <DateTimePicker
                                                required
                                                label="Event Date"
                                                value={eventDate}
                                                onChange={(newValue) => {
                                                    setEventDate(newValue);
                                                }}
                                                renderInput={(params) => <TextField {...params} />}
                                            />
                                        </LocalizationProvider>
                                    </Grid>

                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventQuota(event.target.value)}
                                            required
                                            id="quota"
                                            name="quota"
                                            label="Event Quota"
                                            value={eventQuota}
                                            fullWidth
                                            autoComplete="Event Quota"
                                            type="number" sx={{ m: 1, minWidth: 150 }}
                                            variant="standard"
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6}>
                                        <FormControl sx={{ m: 1, minWidth: 150 }}>

                                            <InputLabel id="dropdown-min-age" sx={{ display: 'inline-block' }}>Minimum Age</InputLabel>
                                            <Select
                                                labelId="dropdown-min-age"
                                                id="dropdown-age"
                                                value={minAge}
                                                onChange={handleMinAgeChange}
                                                autoWidth
                                                label="Min Age"
                                            >

                                                <MenuItem value={"0"}>N/A</MenuItem>
                                                <MenuItem value={"3"}>+3</MenuItem>
                                                <MenuItem value={"6"}>+6</MenuItem>
                                                <MenuItem value={"13"}>+13</MenuItem>
                                                <MenuItem value={"16"}>+16</MenuItem>
                                                <MenuItem value={"18"}>+18</MenuItem>
                                            </Select>
                                        </FormControl>

                                    </Grid>
                                    <Grid item xs={12} sm={6}>
                                        <TextField onChange={event => setEventPrice(event.target.value)}
                                            required
                                            id="price"
                                            name="price"
                                            label="Event Price"
                                            fullWidth
                                            autoComplete="Event Price"
                                            type="number" sx={{ m: 1, minWidth: 150 }}
                                            variant="standard"
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6}>
                                        <FormControl sx={{ m: 1, minWidth: 150 }}>

                                            <InputLabel id="dropdown-refund-po" sx={{ display: 'inline-block' }}>Refund Policy</InputLabel>
                                            <Select
                                                labelId="dropdown-refund-po"
                                                id="dropdown-refund"
                                                value={eventRefundPolicy}
                                                onChange={handleRefPoChange}
                                                autoWidth
                                                label="Refund Policy"
                                            >
                                                <MenuItem value={true}>Allowed</MenuItem>
                                                <MenuItem value={false}>Not Allowed</MenuItem>

                                            </Select>
                                        </FormControl>

                                    </Grid>
                                    <Grid item xs={12}>
                                        <FormHelperText id="description-helper-text">Description</FormHelperText>

                                        <TextareaAutosize onChange={event => setEventDescription(event.target.value)}
                                            maxRows={10}
                                            aria-label="maximum height"

                                            style={{ width: 500 }}
                                        />

                                    </Grid>
                                </Grid>

                            </React.Fragment>
                        </Stack>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => {
                            setOpen(false);
                        }} >Cancel </Button>
                        <Button onClick={() => {
                            createEvent();
                        }} >Submit</Button>
                    </DialogActions>
                </Dialog>
            </div>

            {/**CODE FOR ITEMS**/}
            <div>
                <List sx={{ width: '100%' }}>
                    {rows.map((element, index) => {
                        if (index % 3 === 0) return (
                            <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'center', width: '100%', margin: '20px' }}>
                                <>

                                    {index < rows.length && <OrgEventItem name={rows[index].name} discountPercentage={rows[index].discountPercentage} refundPolicy={rows[index].refundPolicy} eventID={rows[index].eventID} city={rows[index].city} district={rows[index].district} neighbourhood={rows[index].neighbourhood} street={rows[index].street} streetNo={rows[index].streetNo} building={rows[index].building} startDate={rows[index].startDate} description={rows[index].description} quota={rows[index].quota} minimumAge={rows[index].minimumAge} eventType={rows[index].eventType} ticketPrice={rows[index].ticketPrice} />}
                                    {index + 1 < rows.length && <OrgEventItem name={rows[index + 1].name} discountPercentage={rows[index + 1].discountPercentage} refundPolicy={rows[index + 1].refundPolicy} eventID={rows[index + 1].eventID} city={rows[index + 1].city} district={rows[index + 1].district} neighbourhood={rows[index + 1].neighbourhood} street={rows[index + 1].street} streetNo={rows[index + 1].streetNo} building={rows[index + 1].building} startDate={rows[index + 1].startDate} description={rows[index + 1].description} quota={rows[index + 1].quota} minimumAge={rows[index + 1].minimumAge} eventType={rows[index + 1].eventType} ticketPrice={rows[index + 1].ticketPrice} />}
                                    {index + 2 < rows.length && <OrgEventItem name={rows[index + 2].name} discountPercentage={rows[index + 2].discountPercentage} refundPolicy={rows[index + 2].refundPolicy} eventID={rows[index + 2].eventID} city={rows[index + 2].city} district={rows[index + 2].district} neighbourhood={rows[index + 2].neighbourhood} street={rows[index + 2].street} streetNo={rows[index + 2].streetNo} building={rows[index + 2].building} startDate={rows[index + 2].startDate} description={rows[index + 2].description} quota={rows[index + 2].quota} minimumAge={rows[index + 2].minimumAge} eventType={rows[index + 2].eventType} ticketPrice={rows[index + 2].ticketPrice} />}


                                </>
                            </Stack>

                        );
                    })}

                </List>
            </div>
        </>
    );
}