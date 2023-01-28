import * as React from 'react';
import Navbar from "../Navbar/Navbar";
import Stack from '@mui/material/Stack';
import List from '@mui/material/List';
import FavItem from '../Utils/FavItem'
import { Typography } from "@mui/material";
import  {useEffect} from "react";

export default function OrganizedEvents() {

    function createData(eventID, ticketPrice, streetNo, refundPolicy, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate) {
        return { eventID, ticketPrice, streetNo, refundPolicy, description, eventType, city, building, neighbourhood, street, district, quota, minimumAge, name, startDate };
    }

    const [rows, setRows] = React.useState([])
    const [isLoading, setLoading] = React.useState(true);
    useEffect(() => {
        let requestOptions = {
            method: 'GET',
            redirect: 'follow'
        };
        if (isLoading) {
            fetch("http://localhost:8080/getFavoriteEvents?userId=" + localStorage.getItem("userId"), requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        let curBody = resultJSON.body;

                        for (let curEvent of curBody) {
                            rows.push(createData(curEvent.eventId, curEvent.ticketPrice, curEvent.streetNo, curEvent.refundPolicy, curEvent.description, curEvent.eventType, curEvent.city, curEvent.building, curEvent.neighbourhood, curEvent.street, curEvent.district, curEvent.quota, curEvent.minimumAge, curEvent.eventName, curEvent.startDate));
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

    const [eventBuilding, setEventBuilding] = React.useState('');

    const [minAge, setMinAge] = React.useState('');
    const [eventPrice, setEventPrice] = React.useState('');
    const [eventQuota, setEventQuota] = React.useState(null);

    const handleCityChange = (event) => {
        setEventCity(event.target.value);
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
        setMinAge('');
    }

    const createEvent = () => {
        // console.log(eventName, eventAddress, eventDate, eventQuota, eventPrice, eventDescription, eventCountry, eventCity, eventType, eventState, minAge);
        if (eventName === '' || eventType === '' || eventDate === '' || eventCity === '' || eventStreetNo === '' || eventStreet === '' || eventBuilding === '' || eventNeighbourhood === '' || eventDistrict === '' || eventQuota === '' || eventPrice === '' || eventDate === '' || minAge === '') {
            alert("Please fill all the fields");
            cancelDialog();
        }
        else {
            var myHeaders = new Headers();
            myHeaders.append("Content-Type", "application/json");
            let eventTimeReal = eventDate.toISOString().slice(0, 19).replace('T', ' ');

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
              "refundPolicy": true
            });
            
            var requestOptions = {
              method: 'POST',
              headers: myHeaders,
              body: raw,
              redirect: 'follow'
            };
            
            fetch("http://localhost:8080/createEvent", requestOptions)
              .then(response => response.text())
              .then(result => console.log(result))
              .catch(error => console.log('error', error));
        }
    }
    /**END OF INPUT VARIABLE DEFINITIONS **/

    return (
        <>
            <Navbar></Navbar>
            <Typography variant="h6" gutterBottom style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '20px' }}>Favorites</Typography>
            {/**CODE FOR ITEMS**/}
            <div>
                <List sx={{ width: '100%' }}>
                    {rows.map((element, index) => {
                        if (index % 3 === 0) return (
                            <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'center', width: '100%', margin: '20px' }}>
                                <>
                                    {index < rows.length && <FavItem name={rows[index].name} eventID={rows[index].eventID} city={rows[index].city} district={rows[index].district} neighbourhood={rows[index].neighbourhood} streetNo={rows[index].streetNo} building={rows[index].building} startDate={rows[index].startDate} description={rows[index].description} quota={rows[index].quota} minimumAge={rows[index].minimumAge} eventType={rows[index].eventType}/>}
                                    {index + 1 < rows.length && <FavItem name={rows[index + 1].name} eventID={rows[index + 1].eventID} city={rows[index + 1].city} district={rows[index + 1].district} neighbourhood={rows[index + 1].neighbourhood} streetNo={rows[index + 1].streetNo} building={rows[index + 1].building} startDate={rows[index + 1].startDate} description={rows[index + 1].description} quota={rows[index + 1].quota} minimumAge={rows[index + 1].minimumAge} eventType={rows[index + 1].eventType}/>}
                                    {index + 2 < rows.length && <FavItem name={rows[index + 2].name} eventID={rows[index + 2].eventID} city={rows[index + 2].city} district={rows[index + 2].district} neighbourhood={rows[index + 2].neighbourhood} streetNo={rows[index + 2].streetNo} building={rows[index + 2].building} startDate={rows[index + 2].startDate} description={rows[index + 2].description} quota={rows[index + 2].quota} minimumAge={rows[index + 2].minimumAge} eventType={rows[index + 2].eventType}/>}

                                </>
                            </Stack>

                        );
                    })}

                </List>
            </div>
        </>
    );
}