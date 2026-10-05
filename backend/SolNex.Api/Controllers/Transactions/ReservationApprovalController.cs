/*
 * File: ReservationApprovalController.cs
 * Component: Reservation Approval
 * Description:
 * Handles Backoffice approval and rejection of energy reservations.
 */

using System.Security.Claims;

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using SolNex.Api.DTOs.Transactions;
using SolNex.Api.Services.Transactions;

namespace SolNex.Api.Controllers.Transactions;

[ApiController]
[Route("api/reservations")]
[Authorize(Roles = "Backoffice")]
public class ReservationApprovalController :
    ControllerBase
{
    private readonly
        IReservationApprovalService
        _approvalService;

    public ReservationApprovalController(
        IReservationApprovalService approvalService)
    {
        // Store the service used for Backoffice reservation decisions.
        _approvalService =
            approvalService;
    }

    [HttpPut("{id}/approve")]
    public async Task<IActionResult>
        Approve(
            string id)
    {
        // Read the authenticated Backoffice NIC from the JWT.
        var approverNic =
            User.FindFirstValue(
                ClaimTypes
                    .NameIdentifier);

        if (
            string.IsNullOrWhiteSpace(
                approverNic))
        {
            return Unauthorized(
                new
                {
                    message =
                        "Unable to identify the authenticated Backoffice user."
                });
        }

        try
        {
            var result =
                await _approvalService
                    .ApproveAsync(
                        id,
                        approverNic);

            return Ok(
                result);
        }
        catch (
            KeyNotFoundException ex)
        {
            return NotFound(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            InvalidOperationException ex)
        {
            return Conflict(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            ArgumentException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                });
        }
    }

    [HttpPut("{id}/reject")]
    public async Task<IActionResult>
        Reject(
            string id,

            [FromBody]
            RejectReservationDto request)
    {
        // Read the authenticated Backoffice NIC and validate the request.
        var approverNic =
            User.FindFirstValue(
                ClaimTypes
                    .NameIdentifier);

        if (
            string.IsNullOrWhiteSpace(
                approverNic))
        {
            return Unauthorized(
                new
                {
                    message =
                        "Unable to identify the authenticated Backoffice user."
                });
        }

        if (
            !ModelState.IsValid)
        {
            return BadRequest(
                ModelState);
        }

        try
        {
            var result =
                await _approvalService
                    .RejectAsync(
                        id,
                        approverNic,
                        request.Reason);

            return Ok(
                result);
        }
        catch (
            KeyNotFoundException ex)
        {
            return NotFound(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            InvalidOperationException ex)
        {
            return Conflict(
                new
                {
                    message =
                        ex.Message
                });
        }
        catch (
            ArgumentException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                });
        }
    }
}