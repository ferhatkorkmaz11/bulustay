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
import "./Item.css"

export default function FavItem(props) {
    console.log(props)
    const handleFavorited = (event) => {
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
                                    Organizer Name:
                                </Typography>
                                <br />
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
                    <Checkbox icon={<FavoriteBorder />} checkedIcon={<Favorite />} checked={true} onChange={handleFavorited}
                        inputProps={{ 'aria-label': 'controlled' }} />
                </Stack>
            </ListItem>
            <Divider variant="inset" component="li" />
        </>
    )
}