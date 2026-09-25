using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs.Transactions;
using SolNex.Api.Services.Transactions;

namespace SolNex.Api.Controllers.Transactions;

[ApiController]
[Route("api/reservations")]
public class ReservationApprovalController : ControllerBase
{
    private readonly IReservationApprovalService _approvalService;

    // Receives the reservation approval business service.
    public ReservationApprovalController(IReservationApprovalService approvalService)
    {
        _approvalService = approvalService;
    }

    // Approves one pending reservation and creates its energy transaction.
    [HttpPut("{id}/approve")]
    public async Task<IActionResult> Approve(string id, [FromBody] ApproveReservationDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            return Ok(await _approvalService.ApproveAsync(id, request.OperatorNic));
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // Rejects one pending reservation and records the supplied reason.
    [HttpPut("{id}/reject")]
    public async Task<IActionResult> Reject(string id, [FromBody] RejectReservationDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            return Ok(await _approvalService.RejectAsync(id, request.OperatorNic, request.Reason));
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}