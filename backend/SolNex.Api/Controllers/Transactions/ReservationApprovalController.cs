using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using SolNex.Api.DTOs.Transactions;

using SolNex.Api.Services.Transactions;

namespace SolNex.Api.Controllers.Transactions;

[ApiController]
[Route("api/reservations")]
public class ReservationApprovalController :
    ControllerBase
{
    private readonly
        IReservationApprovalService
        _approvalService;

    // Initializes the reservation
    // approval workflow.
    public ReservationApprovalController(
        IReservationApprovalService approvalService)
    {
        _approvalService =
            approvalService;
    }

    // Grid Operator approves
    // one pending reservation.
    [Authorize(
        Roles = "GridOperator")]
    [HttpPut("{id}/approve")]
    public async Task<IActionResult>
        Approve(
            string id,

            [FromBody]
            ApproveReservationDto request)
    {
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
                    .ApproveAsync(
                        id,
                        request.OperatorNic);

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

    // Grid Operator rejects
    // one pending reservation.
    [Authorize(
        Roles = "GridOperator")]
    [HttpPut("{id}/reject")]
    public async Task<IActionResult>
        Reject(
            string id,

            [FromBody]
            RejectReservationDto request)
    {
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
                        request.OperatorNic,
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