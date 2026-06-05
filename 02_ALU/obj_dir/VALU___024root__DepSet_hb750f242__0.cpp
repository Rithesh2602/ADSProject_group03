// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Design implementation internals
// See VALU.h for the primary calling header

#include "VALU__pch.h"
#include "VALU___024root.h"

VL_INLINE_OPT void VALU___024root___ico_sequent__TOP__0(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___ico_sequent__TOP__0\n"); );
    // Body
    vlSelf->io_aluResult = (IData)((0x7fffffffffffffffULL 
                                    & ((0U == (IData)(vlSelf->io_operation))
                                        ? (QData)((IData)(
                                                          (vlSelf->io_operandA 
                                                           + vlSelf->io_operandB)))
                                        : ((1U == (IData)(vlSelf->io_operation))
                                            ? (QData)((IData)(
                                                              (vlSelf->io_operandA 
                                                               - vlSelf->io_operandB)))
                                            : ((2U 
                                                == (IData)(vlSelf->io_operation))
                                                ? (QData)((IData)(
                                                                  (vlSelf->io_operandA 
                                                                   & vlSelf->io_operandB)))
                                                : (
                                                   (3U 
                                                    == (IData)(vlSelf->io_operation))
                                                    ? (QData)((IData)(
                                                                      (vlSelf->io_operandA 
                                                                       | vlSelf->io_operandB)))
                                                    : 
                                                   ((4U 
                                                     == (IData)(vlSelf->io_operation))
                                                     ? (QData)((IData)(
                                                                       (vlSelf->io_operandA 
                                                                        ^ vlSelf->io_operandB)))
                                                     : 
                                                    ((5U 
                                                      == (IData)(vlSelf->io_operation))
                                                      ? 
                                                     ((QData)((IData)(vlSelf->io_operandA)) 
                                                      << 
                                                      (0x1fU 
                                                       & vlSelf->io_operandB))
                                                      : (QData)((IData)(
                                                                        ((6U 
                                                                          == (IData)(vlSelf->io_operation))
                                                                          ? 
                                                                         (vlSelf->io_operandA 
                                                                          >> 
                                                                          (0x1fU 
                                                                           & vlSelf->io_operandB))
                                                                          : 
                                                                         ((7U 
                                                                           == (IData)(vlSelf->io_operation))
                                                                           ? 
                                                                          VL_SHIFTRS_III(32,32,5, vlSelf->io_operandA, 
                                                                                (0x1fU 
                                                                                & vlSelf->io_operandB))
                                                                           : 
                                                                          ((8U 
                                                                            == (IData)(vlSelf->io_operation))
                                                                            ? 
                                                                           (VL_LTS_III(32, vlSelf->io_operandA, vlSelf->io_operandB)
                                                                             ? 1U
                                                                             : 0U)
                                                                            : 
                                                                           ((9U 
                                                                             == (IData)(vlSelf->io_operation))
                                                                             ? 
                                                                            ((vlSelf->io_operandA 
                                                                              < vlSelf->io_operandB)
                                                                              ? 1U
                                                                              : 0U)
                                                                             : 
                                                                            ((0xaU 
                                                                              == (IData)(vlSelf->io_operation))
                                                                              ? vlSelf->io_operandB
                                                                              : 0U)))))))))))))));
}

void VALU___024root___eval_ico(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_ico\n"); );
    // Body
    if ((1ULL & vlSelf->__VicoTriggered.word(0U))) {
        VALU___024root___ico_sequent__TOP__0(vlSelf);
    }
}

void VALU___024root___eval_triggers__ico(VALU___024root* vlSelf);

bool VALU___024root___eval_phase__ico(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_phase__ico\n"); );
    // Init
    CData/*0:0*/ __VicoExecute;
    // Body
    VALU___024root___eval_triggers__ico(vlSelf);
    __VicoExecute = vlSelf->__VicoTriggered.any();
    if (__VicoExecute) {
        VALU___024root___eval_ico(vlSelf);
    }
    return (__VicoExecute);
}

void VALU___024root___eval_act(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_act\n"); );
}

void VALU___024root___eval_nba(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_nba\n"); );
}

void VALU___024root___eval_triggers__act(VALU___024root* vlSelf);

bool VALU___024root___eval_phase__act(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_phase__act\n"); );
    // Init
    VlTriggerVec<0> __VpreTriggered;
    CData/*0:0*/ __VactExecute;
    // Body
    VALU___024root___eval_triggers__act(vlSelf);
    __VactExecute = vlSelf->__VactTriggered.any();
    if (__VactExecute) {
        __VpreTriggered.andNot(vlSelf->__VactTriggered, vlSelf->__VnbaTriggered);
        vlSelf->__VnbaTriggered.thisOr(vlSelf->__VactTriggered);
        VALU___024root___eval_act(vlSelf);
    }
    return (__VactExecute);
}

bool VALU___024root___eval_phase__nba(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_phase__nba\n"); );
    // Init
    CData/*0:0*/ __VnbaExecute;
    // Body
    __VnbaExecute = vlSelf->__VnbaTriggered.any();
    if (__VnbaExecute) {
        VALU___024root___eval_nba(vlSelf);
        vlSelf->__VnbaTriggered.clear();
    }
    return (__VnbaExecute);
}

#ifdef VL_DEBUG
VL_ATTR_COLD void VALU___024root___dump_triggers__ico(VALU___024root* vlSelf);
#endif  // VL_DEBUG
#ifdef VL_DEBUG
VL_ATTR_COLD void VALU___024root___dump_triggers__nba(VALU___024root* vlSelf);
#endif  // VL_DEBUG
#ifdef VL_DEBUG
VL_ATTR_COLD void VALU___024root___dump_triggers__act(VALU___024root* vlSelf);
#endif  // VL_DEBUG

void VALU___024root___eval(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval\n"); );
    // Init
    IData/*31:0*/ __VicoIterCount;
    CData/*0:0*/ __VicoContinue;
    IData/*31:0*/ __VnbaIterCount;
    CData/*0:0*/ __VnbaContinue;
    // Body
    __VicoIterCount = 0U;
    vlSelf->__VicoFirstIteration = 1U;
    __VicoContinue = 1U;
    while (__VicoContinue) {
        if (VL_UNLIKELY((0x64U < __VicoIterCount))) {
#ifdef VL_DEBUG
            VALU___024root___dump_triggers__ico(vlSelf);
#endif
            VL_FATAL_MT("generated-src/ALU.v", 1, "", "Input combinational region did not converge.");
        }
        __VicoIterCount = ((IData)(1U) + __VicoIterCount);
        __VicoContinue = 0U;
        if (VALU___024root___eval_phase__ico(vlSelf)) {
            __VicoContinue = 1U;
        }
        vlSelf->__VicoFirstIteration = 0U;
    }
    __VnbaIterCount = 0U;
    __VnbaContinue = 1U;
    while (__VnbaContinue) {
        if (VL_UNLIKELY((0x64U < __VnbaIterCount))) {
#ifdef VL_DEBUG
            VALU___024root___dump_triggers__nba(vlSelf);
#endif
            VL_FATAL_MT("generated-src/ALU.v", 1, "", "NBA region did not converge.");
        }
        __VnbaIterCount = ((IData)(1U) + __VnbaIterCount);
        __VnbaContinue = 0U;
        vlSelf->__VactIterCount = 0U;
        vlSelf->__VactContinue = 1U;
        while (vlSelf->__VactContinue) {
            if (VL_UNLIKELY((0x64U < vlSelf->__VactIterCount))) {
#ifdef VL_DEBUG
                VALU___024root___dump_triggers__act(vlSelf);
#endif
                VL_FATAL_MT("generated-src/ALU.v", 1, "", "Active region did not converge.");
            }
            vlSelf->__VactIterCount = ((IData)(1U) 
                                       + vlSelf->__VactIterCount);
            vlSelf->__VactContinue = 0U;
            if (VALU___024root___eval_phase__act(vlSelf)) {
                vlSelf->__VactContinue = 1U;
            }
        }
        if (VALU___024root___eval_phase__nba(vlSelf)) {
            __VnbaContinue = 1U;
        }
    }
}

#ifdef VL_DEBUG
void VALU___024root___eval_debug_assertions(VALU___024root* vlSelf) {
    if (false && vlSelf) {}  // Prevent unused
    VALU__Syms* const __restrict vlSymsp VL_ATTR_UNUSED = vlSelf->vlSymsp;
    VL_DEBUG_IF(VL_DBG_MSGF("+    VALU___024root___eval_debug_assertions\n"); );
    // Body
    if (VL_UNLIKELY((vlSelf->clock & 0xfeU))) {
        Verilated::overWidthError("clock");}
    if (VL_UNLIKELY((vlSelf->reset & 0xfeU))) {
        Verilated::overWidthError("reset");}
    if (VL_UNLIKELY((vlSelf->io_operation & 0xf0U))) {
        Verilated::overWidthError("io_operation");}
}
#endif  // VL_DEBUG
