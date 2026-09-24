using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public interface ITransactionRepository
{
    Task<EnergyTransaction?> GetByIdOrTransactionIdAsync(string id);
    Task<EnergyTransaction?> GetByQrTokenAsync(string qrToken);
    Task<EnergyTransaction?> GetByReservationIdAsync(string reservationId);
    Task<IEnumerable<EnergyTransaction>> GetPendingAsync();
    Task<IEnumerable<EnergyTransaction>> GetCompletedAsync();
    Task CreateAsync(EnergyTransaction transaction);
    Task UpdateAsync(EnergyTransaction transaction);
}