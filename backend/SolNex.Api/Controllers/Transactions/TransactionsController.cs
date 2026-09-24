using Microsoft.AspNetCore.Mvc;
using SolNex.Api.DTOs.Transactions;
using SolNex.Api.Services.Transactions;

namespace SolNex.Api.Controllers.Transactions;

[ApiController]
[Route("api/transactions")]
public class TransactionsController : ControllerBase
{
    private readonly ITransactionService _transactionService;

    // Receives the energy transaction business service.
    public TransactionsController(ITransactionService transactionService)
    {
        _transactionService = transactionService;
    }

    // Returns active transactions that are pending or verified.
    [HttpGet("pending")]
    public async Task<IActionResult> GetPending()
    {
        return Ok(await _transactionService.GetPendingAsync());
    }

    // Returns completed transactions for operational history.
    [HttpGet("completed")]
    public async Task<IActionResult> GetCompleted()
    {
        return Ok(await _transactionService.GetCompletedAsync());
    }

    // Returns a single transaction by MongoDB ID or public transaction ID.
    [HttpGet("{id}")]
    public async Task<IActionResult> GetById(string id)
    {
        var transaction = await _transactionService.GetByIdAsync(id);
        return transaction == null
            ? NotFound(new { message = $"Transaction with ID {id} was not found." })
            : Ok(transaction);
    }

    // Verifies a scanned QR token against the server and returns transaction details.
    [HttpPost("verify")]
    public async Task<IActionResult> Verify([FromBody] VerifyTransactionDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            return Ok(await _transactionService.VerifyAsync(request.QrToken, request.OperatorNic));
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

    // Finalises a verified energy transfer and completes its linked reservation.
    [HttpPost("{id}/complete")]
    public async Task<IActionResult> Complete(string id, [FromBody] CompleteTransactionDto request)
    {
        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        try
        {
            return Ok(await _transactionService.CompleteAsync(id, request.OperatorNic));
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