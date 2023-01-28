import React, { useEffect } from "react";
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import { styled } from '@mui/material/styles';
import TableCell, { tableCellClasses } from '@mui/material/TableCell';
import TableRow from '@mui/material/TableRow';

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    [`&.${tableCellClasses.body}`]: {
        fontSize: 14,
    },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': {
        backgroundColor: theme.palette.action.hover,
    },
    // hide last border
    '&:last-child td, &:last-child th': {
        border: 0,
    },
}));

export default function ContentItem(report) {
        const {adminId,title,reportId,content} = report
        const [openContent, setOpenContent] = React.useState(false)
    
        const handleOpenContent = () => {
            setOpenContent(true)
        }
    
        const handleCloseContent = () => {
            setOpenContent(false)
        }

        const handleDelete = () => {
            console.log("aaaaaaaaaaaaaaaaaaaaaaaaa")
            var requestOptions = {
                method: 'DELETE',
                redirect: 'follow'
              };
              
              fetch("http://localhost:8080/deleteReport?reportId=" + reportId, requestOptions)
                .then(response => response.text())
                .then(result => {
                    let resultJSON = JSON.parse(result);
                    if (resultJSON.code === '200') {
                        alert(resultJSON.message)
                        window.location.reload();
                    } else {
                        alert(resultJSON.message)
                    }
                })
                .catch(error => console.log('error', error));
        }
    
        return(
        <StyledTableRow key={reportId}>
            <StyledTableCell>{adminId}</StyledTableCell>
            <StyledTableCell component="th" scope="row">
                {title}
            </StyledTableCell>
            <StyledTableCell>{reportId}</StyledTableCell>
            <StyledTableCell><Button variant="outlined" align="right" onClick={handleOpenContent}>Content</Button></StyledTableCell>
            <StyledTableCell><Button variant="outlined" align="right" onClick={handleDelete}>Delete</Button></StyledTableCell>
            <Dialog fullWidth={true} maxWidth = {'md'} open={openContent} onClose={handleCloseContent}  >
                <DialogContent>
                    <div style={{whiteSpace: "pre-wrap"}}>{content}</div>
                </DialogContent>
                <DialogActions>
                    <Button variant="outlined" onClick={handleCloseContent} > Close </Button>
                </DialogActions>
            </Dialog>
        </StyledTableRow>
        )
    }