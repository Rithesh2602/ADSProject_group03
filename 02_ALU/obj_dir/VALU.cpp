// Verilated -*- C++ -*-
// DESCRIPTION: Verilator output: Model implementation (design independent parts)

#include "VALU__pch.h"

//============================================================
// Constructors

VALU::VALU(VerilatedContext* _vcontextp__, const char* _vcname__)
    : VerilatedModel{*_vcontextp__}
    , vlSymsp{new VALU__Syms(contextp(), _vcname__, this)}
    , clock{vlSymsp->TOP.clock}
    , reset{vlSymsp->TOP.reset}
    , io_operation{vlSymsp->TOP.io_operation}
    , io_operandA{vlSymsp->TOP.io_operandA}
    , io_operandB{vlSymsp->TOP.io_operandB}
    , io_aluResult{vlSymsp->TOP.io_aluResult}
    , rootp{&(vlSymsp->TOP)}
{
    // Register model with the context
    contextp()->addModel(this);
}

VALU::VALU(const char* _vcname__)
    : VALU(Verilated::threadContextp(), _vcname__)
{
}

//============================================================
// Destructor

VALU::~VALU() {
    delete vlSymsp;
}

//============================================================
// Evaluation function

#ifdef VL_DEBUG
void VALU___024root___eval_debug_assertions(VALU___024root* vlSelf);
#endif  // VL_DEBUG
void VALU___024root___eval_static(VALU___024root* vlSelf);
void VALU___024root___eval_initial(VALU___024root* vlSelf);
void VALU___024root___eval_settle(VALU___024root* vlSelf);
void VALU___024root___eval(VALU___024root* vlSelf);

void VALU::eval_step() {
    VL_DEBUG_IF(VL_DBG_MSGF("+++++TOP Evaluate VALU::eval_step\n"); );
#ifdef VL_DEBUG
    // Debug assertions
    VALU___024root___eval_debug_assertions(&(vlSymsp->TOP));
#endif  // VL_DEBUG
    vlSymsp->__Vm_deleter.deleteAll();
    if (VL_UNLIKELY(!vlSymsp->__Vm_didInit)) {
        vlSymsp->__Vm_didInit = true;
        VL_DEBUG_IF(VL_DBG_MSGF("+ Initial\n"););
        VALU___024root___eval_static(&(vlSymsp->TOP));
        VALU___024root___eval_initial(&(vlSymsp->TOP));
        VALU___024root___eval_settle(&(vlSymsp->TOP));
    }
    VL_DEBUG_IF(VL_DBG_MSGF("+ Eval\n"););
    VALU___024root___eval(&(vlSymsp->TOP));
    // Evaluate cleanup
    Verilated::endOfEval(vlSymsp->__Vm_evalMsgQp);
}

//============================================================
// Events and timing
bool VALU::eventsPending() { return false; }

uint64_t VALU::nextTimeSlot() {
    VL_FATAL_MT(__FILE__, __LINE__, "", "%Error: No delays in the design");
    return 0;
}

//============================================================
// Utilities

const char* VALU::name() const {
    return vlSymsp->name();
}

//============================================================
// Invoke final blocks

void VALU___024root___eval_final(VALU___024root* vlSelf);

VL_ATTR_COLD void VALU::final() {
    VALU___024root___eval_final(&(vlSymsp->TOP));
}

//============================================================
// Implementations of abstract methods from VerilatedModel

const char* VALU::hierName() const { return vlSymsp->name(); }
const char* VALU::modelName() const { return "VALU"; }
unsigned VALU::threads() const { return 1; }
void VALU::prepareClone() const { contextp()->prepareClone(); }
void VALU::atClone() const {
    contextp()->threadPoolpOnClone();
}

//============================================================
// Trace configuration

VL_ATTR_COLD void VALU::trace(VerilatedVcdC* tfp, int levels, int options) {
    vl_fatal(__FILE__, __LINE__, __FILE__,"'VALU::trace()' called on model that was Verilated without --trace option");
}
